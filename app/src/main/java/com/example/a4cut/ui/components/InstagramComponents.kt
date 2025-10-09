package com.example.a4cut.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4cut.ui.theme.*

/**
 * 인스타그램 스타일 UI 컴포넌트 모음
 * 인스타그램의 미니멀하고 깔끔한 디자인 시스템을 적용합니다.
 */

/**
 * 인스타그램 스타일 프라이머리 버튼
 * 인스타그램의 파란색 버튼 스타일을 적용합니다.
 */
@Composable
fun InstagramPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) InstagramBlue else TextTertiary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 인스타그램 스타일 세컨더리 버튼
 * 인스타그램의 테두리 버튼 스타일을 적용합니다.
 */
@Composable
fun InstagramSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) TextPrimary else TextTertiary
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (enabled) BorderLight else TextTertiary
        ),
        shape = RoundedCornerShape(8.dp),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (enabled) TextPrimary else TextTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) TextPrimary else TextTertiary
            )
        }
    }
}

/**
 * 인스타그램 스타일 텍스트 버튼
 * 인스타그램의 링크 스타일 텍스트 버튼을 적용합니다.
 */
@Composable
fun InstagramTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = InstagramBlue
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 1f,
        animationSpec = tween(100),
        label = "text_alpha"
    )

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) color else TextTertiary,
            modifier = Modifier.alpha(alpha)
        )
    }
}

/**
 * 인스타그램 스타일 카드
 * 인스타그램의 깔끔한 카드 스타일을 적용합니다.
 */
@Composable
fun InstagramCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = BorderLight
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 인스타그램 스타일 스토리 서클
 * 인스타그램 스토리의 그라데이션 테두리 서클을 적용합니다.
 */
@Composable
fun InstagramStoryCircle(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isViewed: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val storyColors = if (isViewed) {
        listOf(BorderLight, BorderLight)
    } else {
        listOf(StoryGradientStart, StoryGradientEnd)
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(storyColors),
                shape = CircleShape
            )
            .padding(2.dp)
            .background(
                color = SurfaceLight,
                shape = CircleShape
            )
            .padding(2.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 인스타그램 스타일 액션 아이콘
 * 인스타그램의 좋아요, 댓글, 공유 아이콘 스타일을 적용합니다.
 */
@Composable
fun InstagramActionIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = LikeRed,
    inactiveColor: Color = TextPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(100),
        label = "icon_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isActive) activeColor else inactiveColor
        )
    }
}

/**
 * 인스타그램 스타일 구분선
 * 인스타그램의 미묘한 구분선을 적용합니다.
 */
@Composable
fun InstagramDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerLight
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = color)
    )
}

/**
 * 인스타그램 스타일 로딩 인디케이터
 * 인스타그램의 미니멀한 로딩 스피너를 적용합니다.
 */
@Composable
fun InstagramLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = InstagramBlue
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = color,
            strokeWidth = 2.dp
        )
    }
}
