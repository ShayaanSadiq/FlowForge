import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material';

const MAX_DELAY_SECONDS = 7 * 24 * 60 * 60;

function toDateTimeLocalValue(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
    + `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function getDefaultScheduleState() {
  return {
    mode: 'immediate',
    delaySeconds: 30,
    scheduledAtLocal: toDateTimeLocalValue(new Date(Date.now() + 60_000)),
  };
}

export function buildScheduleFields(schedule) {
  if (schedule.mode === 'delay') {
    const delaySeconds = Number(schedule.delaySeconds);
    if (!Number.isFinite(delaySeconds) || delaySeconds < 0) {
      throw new Error('Delay must be a non-negative number of seconds');
    }
    if (delaySeconds > MAX_DELAY_SECONDS) {
      throw new Error('Delay cannot exceed 7 days');
    }
    if (delaySeconds === 0) {
      return {};
    }
    return { delaySeconds };
  }

  if (schedule.mode === 'datetime') {
    if (!schedule.scheduledAtLocal) {
      throw new Error('Choose a run time');
    }
    const scheduledAt = new Date(schedule.scheduledAtLocal);
    if (Number.isNaN(scheduledAt.getTime())) {
      throw new Error('Invalid run time');
    }
    if (scheduledAt.getTime() <= Date.now()) {
      throw new Error('Run time must be in the future');
    }
    return { scheduledAt: scheduledAt.toISOString() };
  }

  return {};
}

export default function JobScheduleForm({ schedule, onChange }) {
  const setField = (field, value) => onChange({ ...schedule, [field]: value });

  return (
    <>
      <Typography variant="subtitle1" sx={{ mt: 2, mb: 1, fontWeight: 600 }}>
        Schedule
      </Typography>
      <FormControl fullWidth margin="normal">
        <InputLabel id="schedule-mode-label">When to run</InputLabel>
        <Select
          labelId="schedule-mode-label"
          label="When to run"
          value={schedule.mode}
          onChange={(e) => setField('mode', e.target.value)}
        >
          <MenuItem value="immediate">Run immediately</MenuItem>
          <MenuItem value="delay">Delay by seconds</MenuItem>
          <MenuItem value="datetime">Run at specific time</MenuItem>
        </Select>
      </FormControl>

      {schedule.mode === 'delay' && (
        <TextField
          fullWidth
          label="Delay (seconds)"
          type="number"
          value={schedule.delaySeconds}
          onChange={(e) => setField('delaySeconds', e.target.value)}
          margin="normal"
          required
          inputProps={{ min: 0, max: MAX_DELAY_SECONDS }}
          helperText="Job stays queued until the delay elapses. Max 7 days."
        />
      )}

      {schedule.mode === 'datetime' && (
        <TextField
          fullWidth
          label="Run at"
          type="datetime-local"
          value={schedule.scheduledAtLocal}
          onChange={(e) => setField('scheduledAtLocal', e.target.value)}
          margin="normal"
          required
          InputLabelProps={{ shrink: true }}
          inputProps={{ min: toDateTimeLocalValue(new Date()) }}
          helperText="Uses your local timezone. Job runs once this time is reached."
        />
      )}
    </>
  );
}

export function isJobScheduled(status, scheduledAt) {
  return status === 'PENDING'
    && scheduledAt
    && new Date(scheduledAt).getTime() > Date.now();
}

export function formatScheduledAt(scheduledAt) {
  if (!scheduledAt) {
    return '-';
  }
  return new Date(scheduledAt).toLocaleString();
}

export function formatScheduleDuration(totalSeconds) {
  const seconds = Math.max(0, Math.round(totalSeconds));
  if (seconds === 0) {
    return 'immediately';
  }

  const days = Math.floor(seconds / 86_400);
  const hours = Math.floor((seconds % 86_400) / 3_600);
  const minutes = Math.floor((seconds % 3_600) / 60);
  const remainingSeconds = seconds % 60;
  const parts = [];

  if (days > 0) {
    parts.push(`${days} day${days === 1 ? '' : 's'}`);
  }
  if (hours > 0) {
    parts.push(`${hours} hour${hours === 1 ? '' : 's'}`);
  }
  if (minutes > 0) {
    parts.push(`${minutes} minute${minutes === 1 ? '' : 's'}`);
  }
  if (remainingSeconds > 0 && days === 0) {
    parts.push(`${remainingSeconds} second${remainingSeconds === 1 ? '' : 's'}`);
  }

  return parts.join(' ');
}

/**
 * Builds a human-readable schedule summary for the submit form preview.
 */
export function describeScheduleSummary(schedule, now = new Date()) {
  if (schedule.mode === 'immediate') {
    return {
      title: 'Run immediately',
      detail: 'The job will be queued as soon as you submit.',
      severity: 'info',
      valid: true,
      runAt: now,
    };
  }

  if (schedule.mode === 'delay') {
    const delaySeconds = Number(schedule.delaySeconds);
    if (!Number.isFinite(delaySeconds) || delaySeconds < 0) {
      return {
        title: 'Invalid delay',
        detail: 'Enter a non-negative number of seconds.',
        severity: 'error',
        valid: false,
        runAt: null,
      };
    }
    if (delaySeconds > MAX_DELAY_SECONDS) {
      return {
        title: 'Delay too long',
        detail: 'Maximum delay is 7 days.',
        severity: 'error',
        valid: false,
        runAt: null,
      };
    }
    if (delaySeconds === 0) {
      return {
        title: 'Run immediately',
        detail: 'A delay of 0 seconds runs as soon as you submit.',
        severity: 'info',
        valid: true,
        runAt: now,
      };
    }

    const runAt = new Date(now.getTime() + delaySeconds * 1_000);
    return {
      title: `Runs in ${formatScheduleDuration(delaySeconds)}`,
      detail: `Scheduled for ${runAt.toLocaleString()} (your local time).`,
      severity: 'info',
      valid: true,
      runAt,
    };
  }

  if (schedule.mode === 'datetime') {
    if (!schedule.scheduledAtLocal) {
      return {
        title: 'No run time selected',
        detail: 'Choose when the job should run.',
        severity: 'warning',
        valid: false,
        runAt: null,
      };
    }

    const runAt = new Date(schedule.scheduledAtLocal);
    if (Number.isNaN(runAt.getTime())) {
      return {
        title: 'Invalid run time',
        detail: 'Choose a valid date and time.',
        severity: 'error',
        valid: false,
        runAt: null,
      };
    }
    if (runAt.getTime() <= now.getTime()) {
      return {
        title: 'Run time is in the past',
        detail: 'Pick a future date and time.',
        severity: 'error',
        valid: false,
        runAt,
      };
    }

    const secondsUntil = Math.round((runAt.getTime() - now.getTime()) / 1_000);
    return {
      title: `Runs at ${runAt.toLocaleString()}`,
      detail: `About ${formatScheduleDuration(secondsUntil)} from now.`,
      severity: 'info',
      valid: true,
      runAt,
    };
  }

  return {
    title: 'Run immediately',
    detail: 'The job will be queued as soon as you submit.',
    severity: 'info',
    valid: true,
    runAt: now,
  };
}
