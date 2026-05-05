/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#FFFDF7',
          100: '#F7F1E3',
          200: '#F0E4BA',
          300: '#F6DF7C',
          500: '#F2D221',
          600: '#EECF33',
          700: '#8A6A00',
        },
        neutral: {
          0: '#FFFFFF',
          25: '#FFFDF9',
          50: '#F4F0E6',
          100: '#EEE7D7',
          200: '#E7DEC9',
          300: '#C7B895',
          500: '#61563D',
          700: '#302516',
          900: '#1E180D',
        },
        success: '#5D8260',
        warning: '#8A6A00',
        danger: '#BA6B52',
        category: {
          ott: '#BA6B52',
          music: '#5D8260',
          ai: '#8A6A00',
          cloud: '#C7B895',
          etc: '#998C71',
        },
      },
      fontFamily: {
        sans: ['Pretendard Variable', 'Pretendard', 'Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        card: '24px',
        input: '22px',
        modal: '24px',
      },
      boxShadow: {
        soft: '0 10px 30px rgba(33, 24, 8, 0.05)',
        card: '0 18px 42px rgba(33, 24, 8, 0.07)',
        floating: '0 22px 46px rgba(33, 24, 8, 0.09)',
      },
      maxWidth: {
        shell: '1760px',
        publicShell: '1800px',
        authShell: '1720px',
      },
    },
  },
  plugins: [],
}
