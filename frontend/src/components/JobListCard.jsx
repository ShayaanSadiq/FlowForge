import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Stack,
  Typography,
} from '@mui/material';
import StatusBadge from './StatusBadge';
import { formatScheduledAt } from './JobScheduleForm';

export default function JobListCard({ job }) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Stack
          direction="row"
          justifyContent="space-between"
          alignItems="flex-start"
          spacing={1}
          sx={{ mb: 1 }}
        >
          <Typography variant="subtitle2" fontWeight={700}>{job.type}</Typography>
          <StatusBadge status={job.status} scheduledAt={job.scheduledAt} />
        </Stack>
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ fontFamily: 'monospace', display: 'block', mb: 1 }}
        >
          ID …{job.id?.slice(-8)}
        </Typography>
        <Box sx={{ display: 'grid', gap: 0.5 }}>
          <Typography variant="body2" color="text.secondary">
            Runs at: {formatScheduledAt(job.scheduledAt)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Attempts: {job.attempts}/{job.maxAttempts}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Created: {job.createdAt ? new Date(job.createdAt).toLocaleString() : '-'}
          </Typography>
        </Box>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2, pt: 0 }}>
        <Button
          component={RouterLink}
          to={`/jobs/${job.id}`}
          size="small"
          variant="contained"
          fullWidth
        >
          View job
        </Button>
      </CardActions>
    </Card>
  );
}
