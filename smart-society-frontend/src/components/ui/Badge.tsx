import { cn } from '@/lib/utils'

interface BadgeProps {
  label: string
  className?: string
  size?: 'sm' | 'md'
}

export function Badge({ label, className, size = 'sm' }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2 font-medium',
        size === 'sm' ? 'py-0.5 text-xs' : 'py-1 text-sm',
        className,
      )}
    >
      {label.replace(/_/g, ' ')}
    </span>
  )
}