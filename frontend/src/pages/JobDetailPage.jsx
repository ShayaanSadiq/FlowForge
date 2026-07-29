import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Paper,
  Typography,
} from '@mui/material';
import { jobsApi } from '../api/client';
import StatusBadge from '../components/StatusBadge';

export default function JobDetailPage() {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadJob = async () => {
    try {
      const data = await jobsApi.get(jobId);
      setJob(data);
      setError('');
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadJob();
    const interval = setInterval(loadJob, 3000);
    return () => clearInterval(interval);
  }, [jobId]);

  const handleRetry = async () => {
    try {
      const updated = await jobsApi.retry(jobId);
      setJob(updated);
    } catch (err) {
      setError(err.message);
    }
  };

  if (!job && !error) {
    return <Typography>Loading job...</Typography>;
  }

  return (
    <Box>
      <Button onClick={() => navigate('/')} sx={{ mb: 2 }}>Back to Dashboard</Button>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {job && (
        <>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
            <Typography variant="h4" fontWeight={700}>Job Detail</Typography>
            <StatusBadge status={job.status} />
            {(job.status === 'FAILED' || job.status === 'DEAD_LETTER') && (
              <Button variant="outlined" onClick={handleRetry}>Retry</Button>
            )}
          </Box>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="body2" color="text.secondary">Job ID</Typography>
            <Typography sx={{ fontFamily: 'monospace', mb: 2 }}>{job.id}</Typography>
            <Typography variant="body2" color="text.secondary">Type</Typography>
            <Typography sx={{ mb: 2 }}>{job.type}</Typography>
            <Typography variant="body2" color="text.secondary">Payload</Typography>
            <Typography sx={{ mb: 2, whiteSpace: 'pre-wrap' }}>{job.payload}</Typography>
            <Typography variant="body2" color="text.secondary">Attempts</Typography>
            <Typography sx={{ mb: 2 }}>{job.attempts} / {job.maxAttempts}</Typography>
            {job.result && (
              <>
                <Typography variant="body2" color="text.secondary">Result</Typography>
                <Typography sx={{ mb: 2 }}>{job.result}</Typography>
              </>
            )}
            {job.errorMessage && (
              <>
                <Typography variant="body2" color="error">Error</Typography>
                <Typography color="error" sx={{ mb: 2 }}>{job.errorMessage}</Typography>
              </>
            )}
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>Execution Logs</Typography>
            <Box component="pre" sx={{
              bgcolor: 'background.default',
              p: 2,
              borderRadius: 1,
              overflow: 'auto',
              maxHeight: 400,
              fontSize: 13,
              fontFamily: 'monospace',
            }}>
              {(job.logs && job.logs.length > 0) ? job.logs.join('\n') : 'No logs yet...'}
            </Box>
          </Paper>
        </>
      )}
    </Box>
  );
}
