import svgPaths from "./svg-nikl5rby5f";
import imgImage3 from "figma:asset/4409302320dc550638b5281cfdf7e016bdfb0d2b.png";
import imgImage1 from "figma:asset/0de8a0e6e4fe893310a2e6a07c63ab7947837061.png";
import img1 from "figma:asset/880781f2a6ba8d1ba752477ce5184be8fbab66cd.png";
import img from "figma:asset/6e29a37a6415df4d87e22334548c34d7785664bf.png";
import img2 from "figma:asset/4230c88428a81b0e87b5148fa73a05ba87640349.png";

function Home() {
  return (
    <div className="absolute bg-[#ffffff] h-[212.875px] left-[853.08px] top-0 w-[616.114px]" data-name="Home">
      <div aria-hidden="true" className="absolute border border-[#000000] border-solid inset-0 pointer-events-none" />
    </div>
  );
}

function Time() {
  return (
    <div
      className="basis-0 box-border content-stretch flex flex-row gap-2.5 grow h-[22px] items-center justify-center min-h-px min-w-px pb-0 pt-0.5 px-0 relative shrink-0"
      data-name="Time"
    >
      <div
        className="font-['SF_Pro:Semibold',_sans-serif] font-[590] leading-[0] relative shrink-0 text-[#000000] text-[17px] text-center text-nowrap"
        style={{ fontVariationSettings: "'wdth' 100" }}
      >
        <p className="block leading-[22px] whitespace-pre">9:41</p>
      </div>
    </div>
  );
}

function Battery() {
  return (
    <div className="h-[13px] relative shrink-0 w-[27.328px]" data-name="Battery">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28 13">
        <g id="Battery">
          <rect
            height="12"
            id="Border"
            opacity="0.35"
            rx="3.8"
            stroke="var(--stroke-0, black)"
            width="24"
            x="0.5"
            y="0.5"
          />
          <path d={svgPaths.p3bbd9700} fill="var(--fill-0, black)" id="Cap" opacity="0.4" />
          <rect fill="var(--fill-0, black)" height="9" id="Capacity" rx="2.5" width="21" x="2" y="2" />
        </g>
      </svg>
    </div>
  );
}

function Levels() {
  return (
    <div
      className="basis-0 box-border content-stretch flex flex-row gap-[7px] grow h-[22px] items-center justify-center min-h-px min-w-px pb-0 pt-px px-0 relative shrink-0"
      data-name="Levels"
    >
      <div className="h-[12.226px] relative shrink-0 w-[19.2px]" data-name="Cellular Connection">
        <svg className="block size-full" fill="none" preserveAspectRatio="none" role="presentation" viewBox="0 0 20 13">
          <path
            clipRule="evenodd"
            d={svgPaths.p1e09e400}
            fill="var(--fill-0, black)"
            fillRule="evenodd"
            id="Cellular Connection"
          />
        </svg>
      </div>
      <div className="h-[12.328px] relative shrink-0 w-[17.142px]" data-name="Wifi">
        <svg className="block size-full" fill="none" preserveAspectRatio="none" role="presentation" viewBox="0 0 18 13">
          <path clipRule="evenodd" d={svgPaths.p1fac3f80} fill="var(--fill-0, black)" fillRule="evenodd" id="Wifi" />
        </svg>
      </div>
      <Battery />
    </div>
  );
}

function StatusBarIPhone() {
  return (
    <div
      className="absolute box-border content-stretch flex flex-row gap-[154px] h-[103.111px] items-center justify-center left-[853.08px] pb-[19px] pt-[21px] px-4 top-0 w-[616.114px]"
      data-name="Status bar - iPhone"
    >
      <Time />
      <Levels />
    </div>
  );
}

function Component() {
  return (
    <div className="absolute h-[19.957px] left-[1085.31px] top-[660.24px] w-[151.659px]" data-name="캐러셀_표시_그룹">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 152 20">
        <g id="ìºë¬ì_íì_ê·¸ë£¹">
          <ellipse cx="42.6541" cy="9.97841" fill="var(--fill-0, #968A8A)" id="Ellipse 1" rx="9.47867" ry="9.97849" />
          <ellipse cx="142.18" cy="9.97841" fill="var(--fill-0, #D9D9D9)" id="Ellipse 5" rx="9.47867" ry="9.97849" />
          <ellipse cx="75.8293" cy="9.97841" fill="var(--fill-0, #D9D9D9)" id="Ellipse 2" rx="9.47867" ry="9.97849" />
          <ellipse cx="109.005" cy="9.97841" fill="var(--fill-0, #D9D9D9)" id="Ellipse 3" rx="9.47867" ry="9.97849" />
          <ellipse cx="9.47874" cy="9.97839" fill="var(--fill-0, #D9D9D9)" id="Ellipse 4" rx="9.47867" ry="9.97849" />
        </g>
      </svg>
    </div>
  );
}

function Group7() {
  return (
    <div className="absolute contents left-[1085.31px] top-[660.24px]">
      <Component />
    </div>
  );
}

function HeroiconsOutlineHome() {
  return (
    <div className="absolute h-[51.556px] left-[891px] top-[1300.53px] w-[48.973px]" data-name="heroicons-outline/home">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
        <g id="heroicons-outline/home">
          <path
            d={svgPaths.p172248e0}
            id="Vector"
            stroke="var(--stroke-0, #0066B3)"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="1.5"
          />
        </g>
      </svg>
    </div>
  );
}

function Component1() {
  return (
    <div className="absolute contents left-[891px] top-[1300.53px]" data-name="홈_그룹">
      <HeroiconsOutlineHome />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[28.272px] leading-[0] left-[915.22px] not-italic text-[#0066b3] text-[14px] text-center top-[1354px] translate-x-[-50%] w-[28.436px]">
        <p className="block leading-[22px]">{`홈 `}</p>
      </div>
    </div>
  );
}

function HeroiconsOutlineCalendarDays() {
  return (
    <div
      className="absolute h-[51.556px] left-[1017.38px] top-[1300.53px] w-[48.973px]"
      data-name="heroicons-outline/calendar-days"
    >
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
        <g id="heroicons-outline/calendar-days">
          <path
            d={svgPaths.p2f3ccc00}
            id="Vector"
            stroke="var(--stroke-0, #0F172A)"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="1.5"
          />
        </g>
      </svg>
    </div>
  );
}

function Component2() {
  return (
    <div className="absolute contents left-[1017.38px] top-[1300.53px]" data-name="달력_그룹">
      <HeroiconsOutlineCalendarDays />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[1042.65px] not-italic text-[#000000] text-[14px] text-center top-[1352.09px] translate-x-[-50%] w-[41.074px]">
        <p className="block leading-[22px]">달력</p>
      </div>
    </div>
  );
}

function HeroiconsOutlineCog6Tooth() {
  return (
    <div
      className="absolute h-[51.556px] left-[1252.77px] top-[1297.2px] w-[48.973px]"
      data-name="heroicons-outline/cog-6-tooth"
    >
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
        <g id="heroicons-outline/cog-6-tooth">
          <g id="Vector">
            <path
              d={svgPaths.p20c6a300}
              stroke="var(--stroke-0, #0F172A)"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="1.5"
            />
            <path
              d={svgPaths.p10e21480}
              stroke="var(--stroke-0, #0F172A)"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="1.5"
            />
          </g>
        </g>
      </svg>
    </div>
  );
}

function Component3() {
  return (
    <div className="absolute contents left-[1252.77px] top-[1297.2px]" data-name="설정_그룹">
      <HeroiconsOutlineCog6Tooth />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[1278.04px] not-italic text-[#000000] text-[14px] text-center top-[1350.42px] translate-x-[-50%] w-[41.074px]">
        <p className="block leading-[22px]">설정</p>
      </div>
    </div>
  );
}

function HeroiconsOutlineUserCircle() {
  return (
    <div
      className="absolute h-[51.556px] left-[1379.15px] top-[1295.54px] w-[48.973px]"
      data-name="heroicons-outline/user-circle"
    >
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
        <g id="heroicons-outline/user-circle">
          <path
            d={svgPaths.p30040a00}
            id="Vector"
            stroke="var(--stroke-0, #0F172A)"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="1.5"
          />
        </g>
      </svg>
    </div>
  );
}

function Component4() {
  return (
    <div className="absolute contents left-[1372.83px] top-[1295.54px]" data-name="프로필_그룹">
      <HeroiconsOutlineUserCircle />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[1403.63px] not-italic text-[#000000] text-[14px] text-center top-[1347.1px] translate-x-[-50%] w-[61.611px]">
        <p className="block leading-[22px]">프로필</p>
      </div>
    </div>
  );
}

function NavBarGroup() {
  return (
    <div className="absolute contents left-[891px] top-[1295.54px]" data-name="nav_bar_group">
      <Component1 />
      <Component2 />
      <Component3 />
      <Component4 />
    </div>
  );
}

function Component5() {
  return (
    <div className="absolute contents left-[853px] top-[683.53px]" data-name="달력_그룹">
      <div
        className="absolute bg-center bg-cover bg-no-repeat h-[36.588px] left-[1410.56px] top-[683.53px] w-[34.749px]"
        data-name="image 3"
        style={{ backgroundImage: `url('${imgImage3}')` }}
      />
      <div className="absolute bg-[#d9d9d9] h-[563.785px] left-[853px] top-[728.43px] w-[616px]">
        <div aria-hidden="true" className="absolute border border-[#000000] border-solid inset-0 pointer-events-none" />
      </div>
      <div className="absolute font-['Inter:Thin',_'Noto_Sans_KR:Regular',_sans-serif] font-thin h-[58.208px] leading-[0] left-[935.92px] not-italic text-[#000000] text-[17px] text-center top-[683.53px] tracking-[-0.408px] translate-x-[-50%] w-[137.415px]">
        <p className="adjustLetterSpacing block leading-[22px]">나의 여행</p>
      </div>
      <div
        className="absolute bg-center bg-cover bg-no-repeat h-[482.294px] left-[862.48px] top-[768.34px] w-[598.626px]"
        data-name="image 1"
        style={{ backgroundImage: `url('${imgImage1}')` }}
      />
      <div className="absolute h-[26.609px] left-[879.85px] top-[947.96px] w-[289.046px]" data-name="Union">
        <div className="absolute bottom-[-30.06%] left-[-1.38%] right-[-1.38%] top-0">
          <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 298 35">
            <g filter="url(#filter0_d_1_468)" id="Union">
              <path d={svgPaths.p2444c900} fill="var(--fill-0, #44DB9A)" />
            </g>
            <defs>
              <filter
                colorInterpolationFilters="sRGB"
                filterUnits="userSpaceOnUse"
                height="34.6094"
                id="filter0_d_1_468"
                width="297.046"
                x="0"
                y="0"
              >
                <feFlood floodOpacity="0" result="BackgroundImageFix" />
                <feColorMatrix
                  in="SourceAlpha"
                  result="hardAlpha"
                  type="matrix"
                  values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                />
                <feOffset dy="4" />
                <feGaussianBlur stdDeviation="2" />
                <feComposite in2="hardAlpha" operator="out" />
                <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0" />
                <feBlend in2="BackgroundImageFix" mode="normal" result="effect1_dropShadow_1_468" />
                <feBlend in="SourceGraphic" in2="effect1_dropShadow_1_468" mode="normal" result="shape" />
              </filter>
            </defs>
          </svg>
        </div>
      </div>
    </div>
  );
}

function Group6() {
  return (
    <div className="absolute h-[98.122px] left-[1113.74px] top-[1242.32px] w-[93.207px]">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 94 99">
        <g id="Group 6">
          <ellipse cx="46.6035" cy="49.0609" fill="var(--fill-0, #D9D9D9)" id="Ellipse 6" rx="46.6035" ry="49.0609" />
          <g id="heroicons-outline/camera">
            <g id="Vector">
              <path
                d={svgPaths.p2525b100}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
              <path
                d={svgPaths.p3364a880}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
              <path
                d={svgPaths.p2f669520}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
            </g>
          </g>
        </g>
      </svg>
    </div>
  );
}

function Component6() {
  return (
    <div className="absolute h-[113.09px] left-[1107.43px] top-[1235.67px] w-[105.845px]" data-name="사진_그룹">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 106 114">
        <g id="ì¬ì§_ê·¸ë£¹">
          <ellipse cx="52.9226" cy="56.5448" fill="var(--fill-0, white)" id="Ellipse 7" rx="52.9226" ry="56.5448" />
          <ellipse cx="52.9222" cy="55.7134" fill="var(--fill-0, #D9D9D9)" id="Ellipse 6" rx="46.6035" ry="49.0609" />
          <g id="heroicons-outline/camera">
            <g id="Vector">
              <path
                d={svgPaths.pe185c0}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
              <path
                d={svgPaths.p5645c80}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
              <path
                d={svgPaths.p32f8e400}
                stroke="var(--stroke-0, #0F172A)"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.5"
              />
            </g>
          </g>
        </g>
      </svg>
    </div>
  );
}

function Component7() {
  return (
    <div className="absolute contents left-[1107.43px] top-[1235.67px]" data-name="사진_그룹">
      <Group6 />
      <Component6 />
    </div>
  );
}

function Image2() {
  return (
    <div className="absolute inset-[8.33%_8.52%_8.53%_8.34%]" data-name="Image 2">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 14 14">
        <g id="Image 2">
          <g id="Group 3">
            <mask
              height="14"
              id="mask0_1_480"
              maskUnits="userSpaceOnUse"
              style={{ maskType: "luminance" }}
              width="14"
              x="0"
              y="0"
            >
              <path
                clipRule="evenodd"
                d={svgPaths.pae95880}
                fill="var(--fill-0, white)"
                fillRule="evenodd"
                id="Clip 2"
              />
            </mask>
            <g mask="url(#mask0_1_480)">
              <path
                clipRule="evenodd"
                d={svgPaths.p1e151300}
                fill="var(--fill-0, #3A00E5)"
                fillRule="evenodd"
                id="Fill 1"
              />
            </g>
          </g>
          <path
            clipRule="evenodd"
            d={svgPaths.p1a28b9e8}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 4"
          />
          <path
            clipRule="evenodd"
            d={svgPaths.p3036e700}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 6"
          />
        </g>
      </svg>
    </div>
  );
}

function IconlyLightOutlineImage4() {
  return (
    <div className="relative shrink-0 size-4" data-name="Iconly/Light-Outline/Image 3">
      <Image2 />
    </div>
  );
}

function Frame613() {
  return (
    <div className="basis-0 box-border content-stretch flex flex-row gap-2.5 grow h-full items-center justify-center min-h-px min-w-px p-0 relative shrink-0">
      <IconlyLightOutlineImage4 />
    </div>
  );
}

function TabIconFixedWidth() {
  return (
    <div
      className="absolute bg-[#f7f5ff] h-[282.724px] left-[1393.37px] top-[246.14px] w-[221.169px]"
      data-name="Tab Icon [Fixed Width]"
    >
      <div className="box-border content-stretch flex flex-row gap-2.5 h-[282.724px] items-center justify-center overflow-clip px-4 py-3 relative w-[221.169px]">
        <Frame613 />
      </div>
      <div
        aria-hidden="true"
        className="absolute border-[#3a00e5] border-[0px_0px_1px] border-solid inset-0 pointer-events-none"
      />
    </div>
  );
}

function Share() {
  return (
    <div className="absolute h-[39.914px] left-[1578.2px] top-[530.52px] w-[37.915px]" data-name="share">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 38 40">
        <g id="share">
          <path d={svgPaths.pb5d700} fill="var(--fill-0, #1D1B20)" id="icon" />
        </g>
      </svg>
    </div>
  );
}

function Group4() {
  return (
    <div className="absolute contents left-[1380.73px] top-[232.83px]">
      <div className="absolute bg-[#bad9fa] h-[377.52px] left-[1380.73px] rounded-[10px] shadow-[4px_4px_4px_0px_rgba(0,0,0,0.25)] top-[232.83px] w-[248.025px]" />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[1393.37px] not-italic text-[#858181] text-[10px] text-left top-[530.52px] tracking-[-0.408px] w-[42.654px]">
        <p className="adjustLetterSpacing block leading-[22px]">서울역</p>
      </div>
      <div className="absolute font-['Inter:Bold',_'Noto_Sans_KR:Bold',_sans-serif] font-bold h-[36.588px] leading-[0] left-[1393.37px] not-italic text-[#000000] text-[12px] text-left top-[563.78px] tracking-[-0.408px] w-[34.755px]">
        <p className="adjustLetterSpacing block leading-[22px]">제목</p>
      </div>
      <TabIconFixedWidth />
      <div className="absolute bg-[#bad9fa] h-[43.24px] left-[1380.73px] rounded-[10px] top-[232.83px] w-[78.989px]" />
      <Share />
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal h-[34.925px] leading-[0] left-[1424.17px] not-italic text-[#000000] text-[10px] text-center top-[236.16px] tracking-[-0.408px] translate-x-[-50%] w-[61.611px]">
        <p className="adjustLetterSpacing block leading-[22px]">16.03.01</p>
      </div>
    </div>
  );
}

function Image3() {
  return (
    <div className="absolute inset-[8.33%_8.52%_8.53%_8.34%]" data-name="Image 2">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 14 14">
        <g id="Image 2">
          <g id="Group 3">
            <mask
              height="14"
              id="mask0_1_480"
              maskUnits="userSpaceOnUse"
              style={{ maskType: "luminance" }}
              width="14"
              x="0"
              y="0"
            >
              <path
                clipRule="evenodd"
                d={svgPaths.pae95880}
                fill="var(--fill-0, white)"
                fillRule="evenodd"
                id="Clip 2"
              />
            </mask>
            <g mask="url(#mask0_1_480)">
              <path
                clipRule="evenodd"
                d={svgPaths.p1e151300}
                fill="var(--fill-0, #3A00E5)"
                fillRule="evenodd"
                id="Fill 1"
              />
            </g>
          </g>
          <path
            clipRule="evenodd"
            d={svgPaths.p1a28b9e8}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 4"
          />
          <path
            clipRule="evenodd"
            d={svgPaths.p3036e700}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 6"
          />
        </g>
      </svg>
    </div>
  );
}

function IconlyLightOutlineImage6() {
  return (
    <div className="relative shrink-0 size-4" data-name="Iconly/Light-Outline/Image 3">
      <Image3 />
    </div>
  );
}

function Frame614() {
  return (
    <div className="basis-0 box-border content-stretch flex flex-row gap-2.5 grow h-full items-center justify-center min-h-px min-w-px p-0 relative shrink-0">
      <IconlyLightOutlineImage6 />
    </div>
  );
}

function TabIconFixedWidth1() {
  return (
    <div
      className="absolute bg-[#f7f5ff] h-[282.724px] left-[1048.97px] top-[246.14px] w-[221.169px]"
      data-name="Tab Icon [Fixed Width]"
    >
      <div className="box-border content-stretch flex flex-row gap-2.5 h-[282.724px] items-center justify-center overflow-clip px-4 py-3 relative w-[221.169px]">
        <Frame614 />
      </div>
      <div
        aria-hidden="true"
        className="absolute border-[#3a00e5] border-[0px_0px_1px] border-solid inset-0 pointer-events-none"
      />
    </div>
  );
}

function Share1() {
  return (
    <div className="absolute h-[39.914px] left-[1233.81px] top-[530.52px] w-[37.915px]" data-name="share">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 38 40">
        <g id="share">
          <path d={svgPaths.pb5d700} fill="var(--fill-0, #1D1B20)" id="icon" />
        </g>
      </svg>
    </div>
  );
}

function Group2() {
  return (
    <div className="absolute contents left-[1036.34px] top-[232.83px]">
      <div className="absolute bg-[#bad9fa] h-[377.52px] left-[1036.34px] rounded-[10px] shadow-[4px_4px_4px_0px_rgba(0,0,0,0.25)] top-[232.83px] w-[248.025px]" />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[1048.97px] not-italic text-[#858181] text-[10px] text-left top-[530.52px] tracking-[-0.408px] w-[42.654px]">
        <p className="adjustLetterSpacing block leading-[22px]">서울역</p>
      </div>
      <div className="absolute font-['Inter:Bold',_'Noto_Sans_KR:Bold',_sans-serif] font-bold h-[36.588px] leading-[0] left-[1048.97px] not-italic text-[#000000] text-[12px] text-left top-[563.78px] tracking-[-0.408px] w-[34.755px]">
        <p className="adjustLetterSpacing block leading-[22px]">제목</p>
      </div>
      <TabIconFixedWidth1 />
      <div className="absolute bg-[#bad9fa] h-[43.24px] left-[1036.34px] rounded-[10px] top-[232.83px] w-[78.989px]" />
      <Share1 />
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal h-[34.925px] leading-[0] left-[1079.78px] not-italic text-[#000000] text-[10px] text-center top-[236.16px] tracking-[-0.408px] translate-x-[-50%] w-[61.611px]">
        <p className="adjustLetterSpacing block leading-[22px]">23.05.05</p>
      </div>
    </div>
  );
}

function Image4() {
  return (
    <div className="absolute inset-[8.33%_8.52%_8.53%_8.34%]" data-name="Image 2">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 14 14">
        <g id="Image 2">
          <g id="Group 3">
            <mask
              height="14"
              id="mask0_1_480"
              maskUnits="userSpaceOnUse"
              style={{ maskType: "luminance" }}
              width="14"
              x="0"
              y="0"
            >
              <path
                clipRule="evenodd"
                d={svgPaths.pae95880}
                fill="var(--fill-0, white)"
                fillRule="evenodd"
                id="Clip 2"
              />
            </mask>
            <g mask="url(#mask0_1_480)">
              <path
                clipRule="evenodd"
                d={svgPaths.p1e151300}
                fill="var(--fill-0, #3A00E5)"
                fillRule="evenodd"
                id="Fill 1"
              />
            </g>
          </g>
          <path
            clipRule="evenodd"
            d={svgPaths.p1a28b9e8}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 4"
          />
          <path
            clipRule="evenodd"
            d={svgPaths.p3036e700}
            fill="var(--fill-0, #3A00E5)"
            fillRule="evenodd"
            id="Fill 6"
          />
        </g>
      </svg>
    </div>
  );
}

function IconlyLightOutlineImage8() {
  return (
    <div className="relative shrink-0 size-4" data-name="Iconly/Light-Outline/Image 3">
      <Image4 />
    </div>
  );
}

function Frame615() {
  return (
    <div className="basis-0 box-border content-stretch flex flex-row gap-2.5 grow h-full items-center justify-center min-h-px min-w-px p-0 relative shrink-0">
      <IconlyLightOutlineImage8 />
    </div>
  );
}

function TabIconFixedWidth2() {
  return (
    <div
      className="absolute bg-[#f7f5ff] h-[282.724px] left-[704.58px] top-[246.14px] w-[221.169px]"
      data-name="Tab Icon [Fixed Width]"
    >
      <div className="box-border content-stretch flex flex-row gap-2.5 h-[282.724px] items-center justify-center overflow-clip px-4 py-3 relative w-[221.169px]">
        <Frame615 />
      </div>
      <div
        aria-hidden="true"
        className="absolute border-[#3a00e5] border-[0px_0px_1px] border-solid inset-0 pointer-events-none"
      />
    </div>
  );
}

function Share2() {
  return (
    <div className="absolute h-[39.914px] left-[887.84px] top-[530.52px] w-[37.915px]" data-name="share">
      <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 38 40">
        <g id="share">
          <path d={svgPaths.pb5d700} fill="var(--fill-0, #1D1B20)" id="icon" />
        </g>
      </svg>
    </div>
  );
}

function Group1() {
  return (
    <div className="absolute contents left-[691.94px] top-[232.83px]">
      <div className="absolute bg-[#bad9fa] h-[377.52px] left-[691.94px] rounded-[10px] shadow-[4px_4px_4px_0px_rgba(0,0,0,0.25)] top-[232.83px] w-[248.025px]" />
      <div className="absolute font-['Inter:Regular',_'Noto_Sans_KR:Regular',_sans-serif] font-normal h-[36.588px] leading-[0] left-[704.58px] not-italic text-[#858181] text-[10px] text-left top-[530.52px] tracking-[-0.408px] w-[71.09px]">
        <p className="adjustLetterSpacing block leading-[22px]">광주송정역</p>
      </div>
      <div className="absolute font-['Inter:Bold',_'Noto_Sans_KR:Bold',_sans-serif] font-bold h-[36.588px] leading-[0] left-[704.58px] not-italic text-[#000000] text-[12px] text-left top-[563.78px] tracking-[-0.408px] w-[34.755px]">
        <p className="adjustLetterSpacing block leading-[22px]">제목</p>
      </div>
      <TabIconFixedWidth2 />
      <div className="absolute bg-[#bad9fa] h-[43.24px] left-[691.94px] rounded-[10px] top-[232.83px] w-[78.989px]" />
      <Share2 />
      <div className="absolute font-['Inter:Regular',_sans-serif] font-normal h-[34.925px] leading-[0] left-[732.23px] not-italic text-[#000000] text-[10px] text-center top-[236.16px] tracking-[-0.408px] translate-x-[-50%] w-[67.93px]">
        <p className="adjustLetterSpacing block leading-[22px]">25.07.18</p>
      </div>
    </div>
  );
}

function Component8() {
  return (
    <div className="absolute contents left-[691.94px] top-[232.83px]" data-name="메인_캐러셀">
      <Group4 />
      <Group2 />
      <Group1 />
    </div>
  );
}

function Component9() {
  return (
    <div className="absolute contents left-0 top-[179.61px]" data-name="전기선">
      <div className="absolute bg-[#000000] h-[58.208px] left-[1157.98px] top-[181.28px] w-[3.16px]" />
      <div className="absolute bg-[#000000] h-[1.663px] left-0 top-[179.61px] w-[2000px]">
        <div
          aria-hidden="true"
          className="absolute border-2 border-[#000000] border-solid inset-0 pointer-events-none"
        />
      </div>
    </div>
  );
}

export default function MainColored() {
  return (
    <div className="relative size-full" data-name="main_colored">
      <Home />
      <div className="absolute bg-[#ffffff] h-[101.448px] left-[853.08px] top-[1290.55px] w-[616.114px]" />
      <StatusBarIPhone />
      <div
        className="absolute bg-center bg-cover bg-no-repeat h-[21.016px] left-[1362.26px] top-[139.97px] w-[86.134px]"
        data-name="코레일 기본로고 1"
        style={{ backgroundImage: `url('${img1}')` }}
      />
      <Group7 />
      <NavBarGroup />
      <div className="absolute h-1.5 left-[881px] top-[1291px] w-[68px]">
        <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 68 6">
          <path d={svgPaths.p51d4d70} fill="var(--fill-0, #0066B3)" id="Rectangle 133" />
        </svg>
      </div>
      <Component5 />
      <Component7 />
      <div
        className="absolute bg-center bg-no-repeat bg-size-[119.72%_123.19%] h-[121.43px] left-[1097.94px] rounded-lg top-[38.17px] w-[124.95px]"
        data-name="로고이미지"
        style={{ backgroundImage: `url('${img}')` }}
      />
      <div
        className="absolute bg-[0%_90.91%] bg-no-repeat bg-size-[100%_135.48%] h-[368.109px] left-0 top-[237.75px] w-[2000px]"
        data-name="기차이미지"
        style={{ backgroundImage: `url('${img2}')` }}
      />
      <Component8 />
      <Component9 />
      <div className="absolute flex flex-col font-['Redacted_Script:Regular',_sans-serif] inset-[40.14%_57.42%_57.47%_38.47%] justify-center leading-[0] not-italic text-[#19191b] text-[14px] text-left tracking-[0.08px]">
        <p className="adjustLetterSpacing block leading-[20px]">filter+1</p>
      </div>
    </div>
  );
}