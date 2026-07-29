import { Chip } from '@mui/material';

const statusColors = {
  PENDING: 'warning',
  RUNNING: 'info',
  SUCCEEDED: 'success',
  FAILED: 'error',
  DEAD_LETTER: 'default',
};

export default function StatusBadge({ status }) {
  return (
    <Chip
      label={status?.replace('_', ' ')}
      color={statusColors[status] || 'default'}
      size="small"
      variant="outlined"
    />
  );
}
