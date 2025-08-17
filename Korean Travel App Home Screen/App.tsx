import { useState } from 'react';
import MainColored from './imports/MainColored';

// Mock data for travel cards
const travelCards = [
  {
    id: 1,
    date: "25.07.18",
    location: "광주송정역",
    title: "제목",
    image: "figma:asset/4409302320dc550638b5281cfdf7e016bdfb0d2b.png"
  },
  {
    id: 2,
    date: "23.05.05", 
    location: "서울역",
    title: "제목",
    image: "figma:asset/0de8a0e6e4fe893310a2e6a07c63ab7947837061.png"
  },
  {
    id: 3,
    date: "16.03.01",
    location: "서울역", 
    title: "제목",
    image: "figma:asset/4409302320dc550638b5281cfdf7e016bdfb0d2b.png"
  }
];

export default function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [currentCardIndex, setCurrentCardIndex] = useState(1); // Middle card is active

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
  };

  const handleCarouselDotClick = (index: number) => {
    setCurrentCardIndex(index);
  };

  return (
    <div className="relative w-full h-screen bg-white overflow-hidden">
      {/* Use the imported design as base */}
      <div className="absolute inset-0">
        <MainColored />
      </div>
      
      {/* Interactive overlay for carousel dots */}
      <div className="absolute left-[1085.31px] top-[660.24px] w-[151.659px] h-[19.957px] z-10">
        <div className="flex justify-between items-center h-full">
          {[0, 1, 2, 3, 4].map((index) => (
            <button
              key={index}
              onClick={() => handleCarouselDotClick(index)}
              className={`w-[18.96px] h-[19.96px] rounded-full transition-colors duration-200 ${
                index === 1 ? 'bg-[#968A8A]' : 'bg-[#D9D9D9]'
              } hover:bg-[#968A8A] focus:outline-none focus:bg-[#968A8A]`}
            />
          ))}
        </div>
      </div>

      {/* Interactive overlay for bottom navigation */}
      <div className="absolute left-[891px] top-[1295.54px] w-[537px] h-[86px] z-10">
        <div className="flex justify-between items-center h-full px-4">
          {/* Home Tab */}
          <button
            onClick={() => handleTabChange('home')}
            className={`flex flex-col items-center justify-center w-[120px] h-full transition-colors duration-200 ${
              activeTab === 'home' ? 'opacity-100' : 'opacity-60 hover:opacity-80'
            }`}
          >
            <div className="w-[48.973px] h-[51.556px] mb-1">
              <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
                <path
                  d="M4.59123 25.7778L22.8633 6.54218C23.7598 5.59841 25.2133 5.59841 26.1098 6.54217L44.3819 25.7778M9.18246 20.9444V42.6944C9.18246 44.0291 10.2102 45.1111 11.4781 45.1111H19.8953V34.6389C19.8953 33.3042 20.9231 32.2222 22.191 32.2222H26.7822C28.05 32.2222 29.0778 33.3042 29.0778 34.6389V45.1111H37.4951C38.7629 45.1111 39.7907 44.0291 39.7907 42.6944V20.9444M16.8345 45.1111H33.669"
                  stroke={activeTab === 'home' ? "#0066B3" : "#0F172A"}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                />
              </svg>
            </div>
            <span className={`text-[14px] ${activeTab === 'home' ? 'text-[#0066B3]' : 'text-[#000000]'}`}>
              홈
            </span>
          </button>

          {/* Calendar Tab */}
          <button
            onClick={() => handleTabChange('calendar')}
            className={`flex flex-col items-center justify-center w-[120px] h-full transition-colors duration-200 ${
              activeTab === 'calendar' ? 'opacity-100' : 'opacity-60 hover:opacity-80'
            }`}
          >
            <div className="w-[48.973px] h-[51.556px] mb-1">
              <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
                <path
                  d="M13.7737 6.44444V11.2778M35.1994 6.44444V11.2778M6.12164 40.2778V16.1111C6.12164 13.4417 8.17721 11.2778 10.7129 11.2778H38.2603C40.7959 11.2778 42.8515 13.4417 42.8515 16.1111V40.2778M6.12164 40.2778C6.12164 42.9472 8.17721 45.1111 10.7129 45.1111H38.2603C40.7959 45.1111 42.8515 42.9472 42.8515 40.2778M6.12164 40.2778V24.1667C6.12164 21.4973 8.17721 19.3333 10.7129 19.3333H38.2603C40.7959 19.3333 42.8515 21.4973 42.8515 24.1667V40.2778"
                  stroke={activeTab === 'calendar' ? "#0066B3" : "#0F172A"}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                />
              </svg>
            </div>
            <span className={`text-[14px] ${activeTab === 'calendar' ? 'text-[#0066B3]' : 'text-[#000000]'}`}>
              달력
            </span>
          </button>

          {/* Settings Tab */}
          <button
            onClick={() => handleTabChange('settings')}
            className={`flex flex-col items-center justify-center w-[120px] h-full transition-colors duration-200 ${
              activeTab === 'settings' ? 'opacity-100' : 'opacity-60 hover:opacity-80'
            }`}
          >
            <div className="w-[48.973px] h-[51.556px] mb-1">
              <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
                <g>
                  <path
                    d="M19.5761 8.46398C19.7606 7.2987 20.7183 6.44462 21.8405 6.44462H27.1336C28.2558 6.44462 29.2135 7.2987 29.398 8.46399L29.8337 11.2159C29.9609 12.0194 30.4709 12.6887 31.1479 13.0832C31.2993 13.1714 31.4489 13.2625 31.5967 13.3564C32.26 13.7781 33.0666 13.9091 33.7922 13.623L36.2753 12.6437C37.3261 12.2292 38.5076 12.6753 39.0687 13.6984L41.7152 18.5241C42.2763 19.5472 42.0526 20.8474 41.1862 21.5982L39.1368 23.3743C38.5399 23.8916 38.244 24.6904 38.2583 25.5041C38.2599 25.5952 38.2607 25.6865 38.2607 25.7779C38.2607 25.8694 38.2599 25.9607 38.2583 26.0517C38.244 26.8655 38.5399 27.6643 39.1368 28.1815L41.1862 29.9577C42.0526 30.7085 42.2763 32.0087 41.7152 33.0318L39.0687 37.8574C38.5076 38.8805 37.3261 39.3266 36.2753 38.9122L33.7922 37.9329C33.0667 37.6467 32.26 37.7778 31.5967 38.1994C31.4489 38.2934 31.2993 38.3845 31.1479 38.4727C30.4709 38.8672 29.9609 39.5365 29.8337 40.34L29.398 43.0919C29.2135 44.2572 28.2558 45.1113 27.1336 45.1113H21.8405C20.7183 45.1113 19.7606 44.2572 19.5761 43.0919L19.1404 40.34C19.0132 39.5365 18.5031 38.8672 17.8262 38.4727C17.6748 38.3845 17.5252 38.2934 17.3774 38.1995C16.7141 37.7778 15.9074 37.6468 15.1819 37.9329L12.6988 38.9122C11.648 39.3267 10.4665 38.8806 9.90541 37.8575L7.25885 33.0318C6.69776 32.0087 6.92151 30.7085 7.78789 29.9577L9.83732 28.1816C10.4342 27.6643 10.7301 26.8655 10.7158 26.0518C10.7142 25.9607 10.7133 25.8694 10.7133 25.7779C10.7133 25.6865 10.7142 25.5952 10.7158 25.5042C10.7301 24.6904 10.4342 23.8916 9.83732 23.3744L7.78789 21.5982C6.92151 20.8474 6.69776 19.5472 7.25885 18.5241L9.90541 13.6985C10.4665 12.6754 11.648 12.2293 12.6988 12.6437L15.1819 13.623C15.9074 13.9092 16.7141 13.7781 17.3774 13.3565C17.5252 13.2625 17.6748 13.1714 17.8262 13.0832C18.5031 12.6887 19.0132 12.0194 19.1404 11.2159L19.5761 8.46398Z"
                    stroke={activeTab === 'settings' ? "#0066B3" : "#0F172A"}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="1.5"
                  />
                  <path
                    d="M30.6082 25.7778C30.6082 29.3369 27.8675 32.2222 24.4866 32.2222C21.1057 32.2222 18.3649 29.3369 18.3649 25.7778C18.3649 22.2186 21.1057 19.3333 24.4866 19.3333C27.8675 19.3333 30.6082 22.2186 30.6082 25.7778Z"
                    stroke={activeTab === 'settings' ? "#0066B3" : "#0F172A"}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="1.5"
                  />
                </g>
              </svg>
            </div>
            <span className={`text-[14px] ${activeTab === 'settings' ? 'text-[#0066B3]' : 'text-[#000000]'}`}>
              설정
            </span>
          </button>

          {/* Profile Tab */}
          <button
            onClick={() => handleTabChange('profile')}
            className={`flex flex-col items-center justify-center w-[120px] h-full transition-colors duration-200 ${
              activeTab === 'profile' ? 'opacity-100' : 'opacity-60 hover:opacity-80'
            }`}
          >
            <div className="w-[48.973px] h-[51.556px] mb-1">
              <svg className="block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 49 52">
                <path
                  d="M36.6922 40.2236C33.8978 36.3412 29.4704 33.8333 24.4866 33.8333C19.5028 33.8333 15.0753 36.3412 12.281 40.2236M36.6922 40.2236C40.4712 36.6825 42.8515 31.5228 42.8515 25.7778C42.8515 15.1003 34.6292 6.44444 24.4866 6.44444C14.3439 6.44444 6.12164 15.1003 6.12164 25.7778C6.12164 31.5228 8.50194 36.6825 12.281 40.2236M36.6922 40.2236C33.4476 43.264 29.172 45.1111 24.4866 45.1111C19.8011 45.1111 15.5255 43.264 12.281 40.2236M30.6082 20.9444C30.6082 24.5036 27.8675 27.3889 24.4866 27.3889C21.1057 27.3889 18.3649 24.5036 18.3649 20.9444C18.3649 17.3853 21.1057 14.5 24.4866 14.5C27.8675 14.5 30.6082 17.3853 30.6082 20.9444Z"
                  stroke={activeTab === 'profile' ? "#0066B3" : "#0F172A"}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                />
              </svg>
            </div>
            <span className={`text-[14px] ${activeTab === 'profile' ? 'text-[#0066B3]' : 'text-[#000000]'}`}>
              프로필
            </span>
          </button>
        </div>
      </div>

      {/* Interactive overlay for camera button */}
      <button 
        className="absolute left-[1107.43px] top-[1235.67px] w-[105.845px] h-[113.09px] z-10 rounded-full transition-transform duration-200 hover:scale-105 focus:outline-none focus:scale-105"
        onClick={() => {
          // Handle camera functionality
          console.log('Camera button clicked');
        }}
      >
        <span className="sr-only">사진 촬영</span>
      </button>

      {/* Interactive overlays for travel cards */}
      <div className="absolute left-[691.94px] top-[232.83px] w-[937.81px] h-[377.52px] z-10">
        <div className="flex gap-4 h-full">
          {travelCards.map((card, index) => (
            <button
              key={card.id}
              onClick={() => {
                setCurrentCardIndex(index);
                console.log(`Clicked on travel card: ${card.location} - ${card.date}`);
              }}
              className={`w-[248.025px] h-full rounded-[10px] transition-all duration-300 hover:scale-[1.02] focus:outline-none focus:scale-[1.02] ${
                index === 1 ? 'shadow-lg' : 'shadow-md hover:shadow-lg'
              }`}
            >
              <span className="sr-only">{`${card.location} 여행 - ${card.date}`}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Blue highlight bar for active home tab */}
      {activeTab === 'home' && (
        <div className="absolute left-[881px] top-[1291px] w-[68px] h-1.5 bg-[#0066B3] rounded-full z-20" />
      )}
    </div>
  );
}