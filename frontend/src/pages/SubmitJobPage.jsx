import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import JobPayloadForm, { buildPayload, getDefaultFormState } from '../components/JobPayloadForm';

const JOB_TYPES = [
  { value: 'PYTHON_SCRIPT', label: 'Python Script' },
  { value: 'HTTP_REQUEST', label: 'HTTP Request' },
  { value: 'JSON_FORMAT', label: 'JSON Format & Validate' },
  { value: 'CSV_ANALYZE', label: 'CSV Analyze' },
  { value: 'DATA_TRANSFORM', label: 'Data Transform' },
  { value: 'HASH_GENERATE', label: 'Hash Generate (SHA-256/512)' },
  { value: 'BASE64_CODEC', label: 'Base64 Encode/Decode' },
];

export default function SubmitJobPage() {
  const [type, setType] = useState('PYTHON_SCRIPT');
  const [form, setForm] = useState(() => getDefaultFormState('PYTHON_SCRIPT'));
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleTypeChange = (newType) => {
    setType(newType);
    setForm(getDefaultFormState(newType));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const payload = buildPayload(type, form);
      const job = await jobsApi.create({ type, payload });
      navigate(`/jobs/${job.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>Submit New Job</Typography>
      <Paper sx={{ p: 3, maxWidth: 720 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
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
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Job'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
