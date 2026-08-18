import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { jobsApi } from '../api/client';
import JobPayloadForm, { buildPayload, getDefaultFormState, parsePayloadToForm } from '../components/JobPayloadForm';
import JobScheduleForm, { buildScheduleFields, describeScheduleSummary, getDefaultScheduleState } from '../components/JobScheduleForm';
import ScheduleSummaryPreview from '../components/ScheduleSummaryPreview';
import { useToast } from '../context/ToastContext';

const JOB_TYPES = [
  { value: 'PYTHON_SCRIPT', label: 'Python Script' },
  { value: 'JSON_FORMAT', label: 'JSON Format & Validate' },
  { value: 'CSV_ANALYZE', label: 'CSV Analyze' },
  { value: 'HASH_GENERATE', label: 'Hash Generate (SHA-256/512)' },
  { value: 'BASE64_CODEC', label: 'Base64 Encode/Decode' },
];

const SUPPORTED_TYPES = new Set(JOB_TYPES.map((option) => option.value));

function readInitialState(location) {
  const duplicate = location.state?.duplicate;
  if (!duplicate?.type || !SUPPORTED_TYPES.has(duplicate.type)) {
    return {
      type: 'PYTHON_SCRIPT',
      form: getDefaultFormState('PYTHON_SCRIPT'),
      schedule: getDefaultScheduleState(),
      fromDuplicate: false,
    };
  }

  try {
    return {
      type: duplicate.type,
      form: parsePayloadToForm(duplicate.type, duplicate.payload ?? ''),
      schedule: getDefaultScheduleState(),
      fromDuplicate: true,
    };
  } catch {
    return {
      type: duplicate.type,
      form: getDefaultFormState(duplicate.type),
      schedule: getDefaultScheduleState(),
      fromDuplicate: true,
    };
  }
}

export default function SubmitJobPage() {
  const location = useLocation();
  const initial = readInitialState(location);
  const [type, setType] = useState(initial.type);
  const [form, setForm] = useState(initial.form);
  const [schedule, setSchedule] = useState(initial.schedule);
  const [fromDuplicate] = useState(initial.fromDuplicate);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { showToast } = useToast();
  const scheduleSummary = useMemo(() => describeScheduleSummary(schedule), [schedule]);

  const handleTypeChange = (newType) => {
    setType(newType);
    setForm(getDefaultFormState(newType));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = buildPayload(type, form);
      const scheduleFields = buildScheduleFields(schedule);
      const job = await jobsApi.create({ type, payload, ...scheduleFields });
      showToast('Job submitted successfully.', 'success');
      navigate(`/jobs/${job.id}`);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>Submit New Job</Typography>
      <Paper sx={{ p: 3, maxWidth: 720 }}>
        {fromDuplicate && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Form pre-filled from a previous job. Adjust anything you need before submitting.
          </Alert>
        )}
        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            select
            fullWidth
            label="Job Type"
            value={type}
            onChange={(e) => handleTypeChange(e.target.value)}
            margin="normal"
          >
            {JOB_TYPES.map((option) => (
              <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
            ))}
          </TextField>
          <JobPayloadForm type={type} form={form} onChange={setForm} />
          <JobScheduleForm schedule={schedule} onChange={setSchedule} />
          <ScheduleSummaryPreview schedule={schedule} />
          <Button
            type="submit"
            variant="contained"
            sx={{ mt: 2 }}
            disabled={loading || !scheduleSummary.valid}
          >
            {loading ? 'Submitting...' : 'Submit Job'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
