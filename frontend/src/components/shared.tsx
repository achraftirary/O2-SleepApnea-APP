import { ReactNode } from 'react'
import clsx from 'clsx'

interface CardProps {
  children: ReactNode
  className?: string
  onClick?: () => void
  hover?: boolean
}

export function Card({ children, className, onClick, hover = true }: CardProps) {
  return (
    <div
      onClick={onClick}
      className={clsx(
        'glass-card panel-glow rounded-[24px] p-6 md:p-7 text-slate-100 transition-all duration-300',
        hover && 'cursor-pointer hover:-translate-y-1 hover:border-sky-300/35 hover:shadow-[0_24px_50px_rgba(0,0,0,0.35)]',
        className
      )}
    >
      {children}
    </div>
  )
}

interface KPICardProps {
  label: string
  value: number | string
  unit?: string
  variant?: 'default' | 'success' | 'warning' | 'danger'
  icon?: ReactNode
}

export function KPICard({ label, value, unit, variant = 'default', icon }: KPICardProps) {
  const colorMap = {
    default: 'from-sky-400/90 to-cyan-300/90 text-sky-50',
    success: 'from-emerald-400/90 to-teal-300/90 text-emerald-50',
    warning: 'from-amber-300/90 to-orange-300/90 text-amber-50',
    danger: 'from-rose-400/90 to-pink-300/90 text-rose-50',
  }

  return (
    <Card className={`border border-white/10 bg-gradient-to-br ${colorMap[variant]} overflow-hidden relative`}>
      <div className="absolute inset-0 bg-white/5 opacity-60" />
      <div className="relative flex items-center justify-between gap-4">
        <div>
          <p className="text-slate-200/70 text-xs uppercase tracking-[0.24em] font-semibold">{label}</p>
          <p className="text-3xl lg:text-4xl font-black mt-2 tracking-tight">
            {value}
            {unit && <span className="text-lg ml-1">{unit}</span>}
          </p>
        </div>
        {icon && <div className="text-4xl lg:text-5xl opacity-35 animate-floatSoft">{icon}</div>}
      </div>
    </Card>
  )
}

interface AlertBannerProps {
  type: 'info' | 'success' | 'warning' | 'danger'
  title: string
  message: string
  onClose?: () => void
}

export function AlertBanner({ type, title, message, onClose }: AlertBannerProps) {
  const borderColorMap = {
    info: 'border-l-sky-400',
    success: 'border-l-emerald-400',
    warning: 'border-l-amber-400',
    danger: 'border-l-rose-400',
  }

  const backgroundMap = {
    info: 'bg-sky-500/10',
    success: 'bg-emerald-500/10',
    warning: 'bg-amber-500/10',
    danger: 'bg-rose-500/10',
  }

  const textMap = {
    info: 'text-sky-100',
    success: 'text-emerald-100',
    warning: 'text-amber-100',
    danger: 'text-rose-100',
  }

  return (
    <div className={`border-l-4 ${borderColorMap[type]} ${backgroundMap[type]} ${textMap[type]} p-4 mb-4 rounded-r-[8px] backdrop-blur-sm`}>
      <div className="flex justify-between items-start gap-4">
        <div>
          <h3 className="font-semibold text-base tracking-tight leading-snug">{title}</h3>
          <p className="text-sm mt-2 text-slate-100/80">{message}</p>
        </div>
        {onClose && (
          <button onClick={onClose} className="text-lg leading-none opacity-40 hover:opacity-80 transition-opacity flex-shrink-0 mt-0.5">
            ×
          </button>
        )}
      </div>
    </div>
  )
}

interface ButtonProps {
  children: ReactNode
  onClick?: () => void
  variant?: 'primary' | 'secondary' | 'danger' | 'success'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  className?: string
}

export function Button({
  children,
  onClick,
  variant = 'primary',
  size = 'md',
  disabled = false,
  className,
}: ButtonProps) {
  const variantMap = {
    primary: 'premium-button premium-button--primary',
    secondary: 'premium-button premium-button--secondary',
    danger: 'premium-button premium-button--danger',
    success: 'premium-button premium-button--success',
  }

  const sizeMap = {
    sm: 'px-3 py-2 text-sm',
    md: 'px-4 py-2.5 text-sm md:text-base',
    lg: 'px-6 py-3 text-base md:text-lg',
  }

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={clsx(
        'rounded-lg font-medium transition-all duration-200 inline-flex items-center justify-center gap-2',
        variantMap[variant],
        sizeMap[size],
        disabled && 'opacity-50 cursor-not-allowed hover:translate-y-0',
        className
      )}
    >
      {children}
    </button>
  )
}

export function Badge({ children, variant = 'primary' }: { children: ReactNode; variant?: 'primary' | 'success' | 'warning' | 'danger' }) {
  const colorMap = {
    primary: 'bg-sky-400/15 text-sky-100 border border-sky-300/20',
    success: 'bg-emerald-400/15 text-emerald-100 border border-emerald-300/20',
    warning: 'bg-amber-400/15 text-amber-100 border border-amber-300/20',
    danger: 'bg-rose-400/15 text-rose-100 border border-rose-300/20',
  }

  return <span className={`inline-flex px-3 py-1 rounded-full text-xs font-bold tracking-[0.14em] uppercase ${colorMap[variant]}`}>{children}</span>
}
