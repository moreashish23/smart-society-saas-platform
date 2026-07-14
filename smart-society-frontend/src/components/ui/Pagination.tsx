import { Button } from './Button'
import { LuChevronLeft, LuChevronRight } from 'react-icons/lu'

interface PaginationProps {
  page: number
  totalPages: number
  totalElements: number
  size: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, totalElements, size, onPageChange }: PaginationProps) {
  const start = page * size + 1
  const end   = Math.min((page + 1) * size, totalElements)

  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-between border-t border-gray-200 pt-4">
      <p className="text-sm text-gray-600">
        Showing <span className="font-medium">{start}–{end}</span> of{' '}
        <span className="font-medium">{totalElements}</span> results
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="outline" size="sm"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          <LuChevronLeft className="h-4 w-4" />
          Previous
        </Button>
        <span className="text-sm text-gray-600">
          Page {page + 1} of {totalPages}
        </span>
        <Button
          variant="outline" size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Next
          <LuChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}