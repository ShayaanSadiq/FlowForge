import {
  Pagination,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import JobListCard from './JobListCard';

export default function JobListCards({
  jobs,
  totalPages,
  page,
  onPageChange,
  compactPagination = false,
}) {
  return (
    <Paper sx={{ p: 2 }}>
      {jobs.length === 0 ? (
        <Typography align="center" color="text.secondary">
          No jobs match the current filters.
        </Typography>
      ) : (
        <Stack spacing={2}>
          {jobs.map((job) => (
            <JobListCard key={job.id} job={job} />
          ))}
        </Stack>
      )}

      {totalPages > 1 && (
        <Stack direction="row" justifyContent="center" sx={{ pt: 2 }}>
          <Pagination
            count={totalPages}
            page={page + 1}
            onChange={onPageChange}
            color="primary"
            size={compactPagination ? 'small' : 'medium'}
            siblingCount={compactPagination ? 0 : 1}
            boundaryCount={compactPagination ? 1 : 2}
            showFirstButton={!compactPagination}
            showLastButton={!compactPagination}
          />
        </Stack>
      )}
    </Paper>
  );
}
