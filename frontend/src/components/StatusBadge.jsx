import { Chip } from '@mui/material';

const statusColors = {
  PENDING: 'warning',
  RUNNING: 'info',
  SUCCEEDED: 'success',
  FAILED: 'error',
  DEAD_LETTER: 'default',
};

export default function StatusBadge({ status, scheduledAt }) {
  const isScheduled = status === 'PENDING'
    && scheduledAt
    && new Date(scheduledAt).getTime() > Date.now();
  const label = isScheduled ? 'SCHEDULED' : status?.replace('_', ' ');
  const color = isScheduled ? 'secondary' : (statusColors[status] || 'default');

  return (
    <Chip
      label={label}
      color={color}
      size="small"
      variant="outlined"
    />
  );
}
