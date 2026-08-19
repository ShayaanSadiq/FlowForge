import { Link as RouterLink } from 'react-router-dom';
import {
  Button,
  Pagination,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material';
import StatusBadge from './StatusBadge';
import { formatScheduledAt } from './JobScheduleForm';

export default function JobListTable({ jobs, totalPages, page, onPageChange }) {
  return (
    <Paper>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Runs at</TableCell>
            <TableCell>Attempts</TableCell>
            <TableCell>Created</TableCell>
            <TableCell />
          </TableRow>
        </TableHead>
        <TableBody>
          {jobs.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} align="center">
                No jobs match the current filters.
              </TableCell>
            </TableRow>
          ) : jobs.map((job) => (
            <TableRow key={job.id} hover>
              <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                {job.id?.slice(-8)}
              </TableCell>
              <TableCell>{job.type}</TableCell>
              <TableCell>
                <StatusBadge status={job.status} scheduledAt={job.scheduledAt} />
              </TableCell>
              <TableCell>{formatScheduledAt(job.scheduledAt)}</TableCell>
              <TableCell>{job.attempts}/{job.maxAttempts}</TableCell>
              <TableCell>
                {job.createdAt ? new Date(job.createdAt).toLocaleString() : '-'}
              </TableCell>
              <TableCell>
                <Button component={RouterLink} to={`/jobs/${job.id}`} size="small">
                  View
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {totalPages > 1 && (
        <Stack direction="row" justifyContent="center" sx={{ py: 2 }}>
          <Pagination
            count={totalPages}
            page={page + 1}
            onChange={onPageChange}
            color="primary"
            showFirstButton
            showLastButton
          />
        </Stack>
      )}
    </Paper>
  );
}
