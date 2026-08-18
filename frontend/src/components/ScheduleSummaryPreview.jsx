import { Alert, Typography } from '@mui/material';
import { describeScheduleSummary } from './JobScheduleForm';

export default function ScheduleSummaryPreview({ schedule }) {
  const summary = describeScheduleSummary(schedule);

  return (
    <Alert severity={summary.severity} sx={{ mt: 2 }}>
      <Typography variant="subtitle2" fontWeight={700}>
        {summary.title}
      </Typography>
      <Typography variant="body2">{summary.detail}</Typography>
    </Alert>
  );
}
