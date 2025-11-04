# KTX 네컷 앱 테스트 자동 검증 스크립트
# 작성일: 2025년 1월 14일

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "KTX 네컷 앱 테스트 자동 검증 시작" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$testResults = @()
$totalTests = 0
$passedTests = 0

# 1. 빌드 확인
Write-Host "[1/8] 빌드 확인 중..." -ForegroundColor Yellow
$totalTests++
try {
    $buildOutput = .\gradlew assembleDebug --no-daemon 2>&1 | Select-String -Pattern "BUILD"
    if ($LASTEXITCODE -eq 0) {
        $testResults += @{Category="빌드"; Test="디버그 APK 빌드"; Status="✅ 성공"; Details=""}
        $passedTests++
        Write-Host "  ✅ 빌드 성공" -ForegroundColor Green
    } else {
        $testResults += @{Category="빌드"; Test="디버그 APK 빌드"; Status="❌ 실패"; Details="빌드 오류 발생"}
        Write-Host "  ❌ 빌드 실패" -ForegroundColor Red
    }
} catch {
    $testResults += @{Category="빌드"; Test="디버그 APK 빌드"; Status="❌ 오류"; Details=$_.Exception.Message}
    Write-Host "  ❌ 오류: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. APK 파일 확인
Write-Host "[2/8] APK 파일 확인 중..." -ForegroundColor Yellow
$totalTests++
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
    $testResults += @{Category="빌드"; Test="APK 파일 생성"; Status="✅ 성공"; Details="$apkSize MB"}
    $passedTests++
    Write-Host "  ✅ APK 파일 존재 ($apkSize MB)" -ForegroundColor Green
} else {
    $testResults += @{Category="빌드"; Test="APK 파일 생성"; Status="❌ 실패"; Details="파일 없음"}
    Write-Host "  ❌ APK 파일 없음" -ForegroundColor Red
}

# 3. 필수 파일 확인
Write-Host "[3/8] 필수 파일 확인 중..." -ForegroundColor Yellow
$requiredFiles = @(
    @{Path="app\src\main\assets\frames.json"; Name="JSON 프레임 파일"; Critical=$true},
    @{Path="app\src\main\java\com\example\a4cut\ui\utils\VideoSlideShowCreator.kt"; Name="동영상 생성 모듈"; Critical=$true},
    @{Path="app\src\main\java\com\example\a4cut\ui\screens\CropScreen.kt"; Name="크롭 화면"; Critical=$true},
    @{Path="app\src\main\java\com\example\a4cut\ui\utils\FrameSlotCalculator.kt"; Name="슬롯 계산기"; Critical=$true},
    @{Path="app\src\main\AndroidManifest.xml"; Name="AndroidManifest"; Critical=$true}
)

foreach ($file in $requiredFiles) {
    $totalTests++
    if (Test-Path $file.Path) {
        $testResults += @{Category="파일"; Test=$file.Name; Status="✅ 존재"; Details=""}
        $passedTests++
        Write-Host "  ✅ $($file.Name)" -ForegroundColor Green
    } else {
        $status = if ($file.Critical) { "❌ 없음 (필수)" } else { "⚠️ 없음" }
        $testResults += @{Category="파일"; Test=$file.Name; Status=$status; Details=""}
        if ($file.Critical) {
            Write-Host "  ❌ $($file.Name) - 필수 파일 없음!" -ForegroundColor Red
        } else {
            Write-Host "  ⚠️ $($file.Name) - 파일 없음" -ForegroundColor Yellow
        }
    }
}

# 4. JSON 파일 구조 검증
Write-Host "[4/8] JSON 파일 구조 검증 중..." -ForegroundColor Yellow
$totalTests++
$jsonPath = "app\src\main\assets\frames.json"
if (Test-Path $jsonPath) {
    try {
        $jsonContent = Get-Content $jsonPath -Raw | ConvertFrom-Json
        $frameCount = $jsonContent.frames.Count
        $framesWithSlots = ($jsonContent.frames | Where-Object { $_.slots -ne $null }).Count
        
        if ($frameCount -gt 0 -and $framesWithSlots -gt 0) {
            $testResults += @{Category="JSON"; Test="JSON 파일 구조"; Status="✅ 유효"; Details="$frameCount개 프레임, $framesWithSlots개 슬롯 정보"}
            $passedTests++
            Write-Host "  ✅ JSON 구조 유효 ($frameCount개 프레임, $framesWithSlots개 슬롯 정보)" -ForegroundColor Green
        } else {
            $testResults += @{Category="JSON"; Test="JSON 파일 구조"; Status="⚠️ 부분적"; Details="프레임 또는 슬롯 정보 부족"}
            Write-Host "  ⚠️ JSON 구조 부분적 (프레임 또는 슬롯 정보 부족)" -ForegroundColor Yellow
        }
    } catch {
        $testResults += @{Category="JSON"; Test="JSON 파일 구조"; Status="❌ 오류"; Details=$_.Exception.Message}
        Write-Host "  ❌ JSON 파싱 오류: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    $testResults += @{Category="JSON"; Test="JSON 파일 구조"; Status="❌ 없음"; Details="파일 없음"}
    Write-Host "  ❌ JSON 파일 없음" -ForegroundColor Red
}

# 5. AndroidManifest 권한 확인
Write-Host "[5/8] AndroidManifest 권한 확인 중..." -ForegroundColor Yellow
$totalTests++
$manifestPath = "app\src\main\AndroidManifest.xml"
if (Test-Path $manifestPath) {
    $manifestContent = Get-Content $manifestPath -Raw
    $hasReadMediaImages = $manifestContent -match "READ_MEDIA_IMAGES"
    $hasOnBackInvoked = $manifestContent -match "enableOnBackInvokedCallback"
    
    if ($hasReadMediaImages -and $hasOnBackInvoked) {
        $testResults += @{Category="Manifest"; Test="권한 및 설정"; Status="✅ 완료"; Details="READ_MEDIA_IMAGES, OnBackInvokedCallback"}
        $passedTests++
        Write-Host "  ✅ 필요한 권한 및 설정 확인됨" -ForegroundColor Green
    } else {
        $missing = @()
        if (-not $hasReadMediaImages) { $missing += "READ_MEDIA_IMAGES" }
        if (-not $hasOnBackInvoked) { $missing += "enableOnBackInvokedCallback" }
        $testResults += @{Category="Manifest"; Test="권한 및 설정"; Status="⚠️ 부분적"; Details="누락: $($missing -join ', ')"}
        Write-Host "  ⚠️ 일부 권한/설정 누락: $($missing -join ', ')" -ForegroundColor Yellow
    }
} else {
    $testResults += @{Category="Manifest"; Test="권한 및 설정"; Status="❌ 없음"; Details="파일 없음"}
    Write-Host "  ❌ AndroidManifest.xml 없음" -ForegroundColor Red
}

# 6. 핵심 클래스 구현 확인
Write-Host "[6/8] 핵심 클래스 구현 확인 중..." -ForegroundColor Yellow
$coreClasses = @(
    @{Path="app\src\main\java\com\example\a4cut\ui\viewmodel\FrameViewModel.kt"; Name="FrameViewModel"; Methods=@("setContext", "loadSlotsFromJson")},
    @{Path="app\src\main\java\com\example\a4cut\data\repository\FrameRepository.kt"; Name="FrameRepository"; Methods=@("loadSlotsFromJson")},
    @{Path="app\src\main\java\com\example\a4cut\ui\utils\ImageComposer.kt"; Name="ImageComposer"; Methods=@("composeLife4CutFrame")}
)

foreach ($class in $coreClasses) {
    $totalTests++
    if (Test-Path $class.Path) {
        $fileContent = Get-Content $class.Path -Raw
        $methodsFound = $class.Methods | Where-Object { $fileContent -match $_ }
        $foundCount = $methodsFound.Count
        
        if ($foundCount -eq $class.Methods.Count) {
            $testResults += @{Category="코드"; Test="$($class.Name) 구현"; Status="✅ 완료"; Details="모든 메서드 구현됨"}
            $passedTests++
            Write-Host "  ✅ $($class.Name) - 모든 메서드 구현됨" -ForegroundColor Green
        } else {
            $missing = $class.Methods | Where-Object { $fileContent -notmatch $_ }
            $testResults += @{Category="코드"; Test="$($class.Name) 구현"; Status="⚠️ 부분적"; Details="누락: $($missing -join ', ')"}
            Write-Host "  ⚠️ $($class.Name) - 일부 메서드 누락: $($missing -join ', ')" -ForegroundColor Yellow
        }
    } else {
        $testResults += @{Category="코드"; Test="$($class.Name) 구현"; Status="❌ 없음"; Details="파일 없음"}
        Write-Host "  ❌ $($class.Name) - 파일 없음" -ForegroundColor Red
    }
}

# 7. 최근 수정 사항 확인
Write-Host "[7/8] 최근 수정 사항 확인 중..." -ForegroundColor Yellow
$totalTests++
$homeViewModelPath = "app\src\main\java\com\example\a4cut\ui\viewmodel\HomeViewModel.kt"
if (Test-Path $homeViewModelPath) {
    $content = Get-Content $homeViewModelPath -Raw
    $hasRunBlock = $content -match "val filteredPhotosForMap.*=.*run\s*\{"
    $hasDatesWithPhotos = $content -match "val datesWithPhotos.*=.*run\s*\{"
    
    if ($hasRunBlock -and $hasDatesWithPhotos) {
        $testResults += @{Category="수정사항"; Test="StateFlow 안전 초기화"; Status="✅ 완료"; Details="run 블록으로 안전 초기화"}
        $passedTests++
        Write-Host "  ✅ StateFlow 안전 초기화 확인됨" -ForegroundColor Green
    } else {
        $testResults += @{Category="수정사항"; Test="StateFlow 안전 초기화"; Status="⚠️ 부분적"; Details="일부만 수정됨"}
        Write-Host "  ⚠️ StateFlow 안전 초기화 부분적" -ForegroundColor Yellow
    }
} else {
    $testResults += @{Category="수정사항"; Test="StateFlow 안전 초기화"; Status="❌ 확인 불가"; Details="파일 없음"}
    Write-Host "  ❌ HomeViewModel 파일 없음" -ForegroundColor Red
}

# 8. 결과 요약
Write-Host "[8/8] 결과 요약 중..." -ForegroundColor Yellow
$totalTests++

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "테스트 결과 요약" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 카테고리별 통계
$categories = $testResults | Group-Object Category
foreach ($category in $categories) {
    Write-Host "[$($category.Name)]" -ForegroundColor Cyan
    $category.Group | Format-Table -Property Test, Status, Details -AutoSize
    Write-Host ""
}

# 전체 통계
$successRate = [math]::Round(($passedTests / $totalTests) * 100, 1)
Write-Host "전체 통계:" -ForegroundColor Yellow
Write-Host "  총 테스트: $totalTests" -ForegroundColor White
Write-Host "  통과: $passedTests" -ForegroundColor Green
Write-Host "  실패: $($totalTests - $passedTests)" -ForegroundColor Red
Write-Host "  통과율: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "테스트 완료" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 결과를 파일로 저장
$testResults | Export-Csv -Path "test_results.csv" -NoTypeInformation -Encoding UTF8
Write-Host "`n결과가 test_results.csv에 저장되었습니다." -ForegroundColor Green

