/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        fondo: '#FAF5FF',
        fondoSuave: '#FDF2F8',
        superficie: '#FFFFFF',
        textoPrincipal: '#2E1065',
        textoSecundario: '#6B5B7A',
        acento: '#A855F7',
        acentoHover: '#9333EA',
        rosa: '#EC4899',
        rosaHover: '#DB2777',
        morado: '#7C3AED',
        borde: '#E9D5FF',
        bordeActivo: '#D8B4FE',
        exito: '#059669',
        error: '#E11D48',
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'Inter', 'system-ui', 'sans-serif'],
      },
      fontSize: {
        xs: ['12px', { lineHeight: '1.5' }],
        sm: ['14px', { lineHeight: '1.5' }],
        base: ['16px', { lineHeight: '1.5' }],
        lg: ['18px', { lineHeight: '1.5' }],
        xl: ['20px', { lineHeight: '1.2' }],
        '2xl': ['24px', { lineHeight: '1.2' }],
        '3xl': ['30px', { lineHeight: '1.2' }],
        '4xl': ['36px', { lineHeight: '1.2' }],
        '5xl': ['48px', { lineHeight: '1.2' }],
      },
      spacing: {
        0: '0px',
        1: '4px',
        2: '8px',
        3: '12px',
        4: '16px',
        5: '20px',
        6: '24px',
        8: '32px',
        10: '40px',
        12: '48px',
        16: '64px',
        20: '80px',
      },
      boxShadow: {
        sutil: '0 1px 3px 0 rgb(126 34 206 / 0.06)',
        media: '0 4px 12px -2px rgb(168 85 247 / 0.12)',
        flotante: '0 12px 24px -8px rgb(168 85 247 / 0.18)',
        acento: '0 4px 16px rgb(168 85 247 / 0.28)',
      },
      borderRadius: {
        sm: '6px',
        md: '10px',
        lg: '14px',
        xl: '18px',
      },
      backgroundImage: {
        'gradient-app': 'linear-gradient(145deg, #FAF5FF 0%, #FDF2F8 45%, #F5F3FF 100%)',
        'gradient-brand': 'linear-gradient(135deg, #A855F7 0%, #EC4899 100%)',
        'gradient-sidebar': 'linear-gradient(180deg, #FFFFFF 0%, #FAF5FF 100%)',
      },
      transitionTimingFunction: {
        smooth: 'cubic-bezier(0.2, 0, 0, 1)',
      },
      transitionDuration: {
        150: '150ms',
        200: '200ms',
        250: '250ms',
      },
      keyframes: {
        toastEnter: {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        toastExit: {
          '0%': { opacity: '1' },
          '100%': { opacity: '0' },
        },
        pageEnter: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        tooltipEnter: {
          '0%': { transform: 'translateY(4px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
      animation: {
        toastEnter: 'toastEnter 250ms cubic-bezier(0.2, 0, 0, 1) forwards',
        toastExit: 'toastExit 150ms cubic-bezier(0.2, 0, 0, 1) forwards',
        pageEnter: 'pageEnter 300ms cubic-bezier(0.2, 0, 0, 1) forwards',
        tooltipEnter: 'tooltipEnter 150ms 200ms cubic-bezier(0.2, 0, 0, 1) forwards',
        shimmer: 'shimmer 1.5s ease-in-out infinite',
      },
    },
  },
  plugins: [],
};
