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

const JOB_TYPES = [
  { value: 'SIMULATION', label: 'Simulation' },
  { value: 'DATA_TRANSFORM', label: 'Data Transform' },
  { value: 'REPORT_GENERATION', label: 'Report Generation' },
  { value: 'PYTHON_SCRIPT', label: 'Python Script (simulated)' },
];

export default function SubmitJobPage() {
  const [type, setType] = useState('SIMULATION');
  const [payload, setPayload] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
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
      <Paper sx={{ p: 3, maxWidth: 640 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            select
            fullWidth
            label="Job Type"
            value={type}
            onChange={(e) => setType(e.target.value)}
            margin="normal"
          >
            {JOB_TYPES.map((option) => (
              <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            fullWidth
            label="Payload"
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            margin="normal"
            multiline
            rows={4}
            required
            helperText="Input data, script name, or parameters for the job"
          />
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Job'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
