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
import { formatScheduledAt, isJobScheduled } from '../components/JobScheduleForm';

const PRE_BLOCK_SX = {
  bgcolor: 'background.default',
  p: 2,
  borderRadius: 1,
  overflow: 'auto',
  maxWidth: '100%',
  maxHeight: 400,
  fontSize: 13,
  fontFamily: 'monospace',
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
  overflowWrap: 'anywhere',
  m: 0,
};

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
            <StatusBadge status={job.status} scheduledAt={job.scheduledAt} />
            {(job.status === 'FAILED' || job.status === 'DEAD_LETTER') && (
              <Button variant="outlined" onClick={handleRetry}>Retry</Button>
            )}
          </Box>

          <Paper sx={{ p: 3, mb: 3, overflow: 'hidden' }}>
            <Typography variant="body2" color="text.secondary">Job ID</Typography>
            <Box component="pre" sx={{ ...PRE_BLOCK_SX, maxHeight: 80, mb: 2 }}>{job.id}</Box>
            <Typography variant="body2" color="text.secondary">Type</Typography>
            <Typography sx={{ mb: 2 }}>{job.type}</Typography>
            <Typography variant="body2" color="text.secondary">Payload</Typography>
            <Box component="pre" sx={{ ...PRE_BLOCK_SX, mb: 2 }}>{job.payload}</Box>
            <Typography variant="body2" color="text.secondary">Attempts</Typography>
            <Typography sx={{ mb: 2 }}>{job.attempts} / {job.maxAttempts}</Typography>
            <Typography variant="body2" color="text.secondary">Scheduled for</Typography>
            <Typography sx={{ mb: 2 }}>
              {isJobScheduled(job.status, job.scheduledAt)
                ? formatScheduledAt(job.scheduledAt)
                : job.scheduledAt
                  ? `${formatScheduledAt(job.scheduledAt)} (ready)`
                  : 'Immediately'}
            </Typography>
            {job.result && (
              <>
                <Typography variant="body2" color="text.secondary">Result</Typography>
                <Box component="pre" sx={{ ...PRE_BLOCK_SX, mb: 2 }}>{job.result}</Box>
              </>
            )}
            {job.errorMessage && (
              <>
                <Typography variant="body2" color="error">Error</Typography>
                <Box component="pre" sx={{ ...PRE_BLOCK_SX, mb: 2, color: 'error.main' }}>{job.errorMessage}</Box>
              </>
            )}
          </Paper>

          <Paper sx={{ p: 3, overflow: 'hidden' }}>
            <Typography variant="h6" gutterBottom>Execution Logs</Typography>
            <Box component="pre" sx={PRE_BLOCK_SX}>
              {(job.logs && job.logs.length > 0) ? job.logs.join('\n') : 'No logs yet...'}
            </Box>
          </Paper>
        </>
      )}
    </Box>
  );
}
