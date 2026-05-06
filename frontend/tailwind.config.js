/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'medical-blue': '#0066cc',
        'medical-dark': '#1a1a2e',
        'medical-accent': '#16a34a',
        'medical-warning': '#f59e0b',
        'medical-danger': '#dc2626',
      },
      spacing: {
        '128': '32rem',
      },
    },
  },
  plugins: [],
}
