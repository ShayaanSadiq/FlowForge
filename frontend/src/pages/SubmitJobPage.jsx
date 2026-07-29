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
  { value: 'PYTHON_SCRIPT', label: 'Python Script' },
  { value: 'HTTP_REQUEST', label: 'HTTP Request' },
  { value: 'JSON_FORMAT', label: 'JSON Format & Validate' },
  { value: 'CSV_ANALYZE', label: 'CSV Analyze' },
  { value: 'DATA_TRANSFORM', label: 'Data Transform' },
  { value: 'HASH_GENERATE', label: 'Hash Generate (SHA-256/512)' },
  { value: 'BASE64_CODEC', label: 'Base64 Encode/Decode' },
];

const EXAMPLES = {
  PYTHON_SCRIPT: `print("Hello from FlowForge")

for i in range(3):
    print(f"step {i}")`,
  HTTP_REQUEST: `{
  "url": "https://api.github.com",
  "method": "GET"
}`,
  JSON_FORMAT: `{
  "name": "FlowForge",
  "features": ["jobs", "worker", "mongodb"]
}`,
  CSV_ANALYZE: `name,email,score
Alice,alice@example.com,92
Bob,bob@example.com,
Carol,carol@example.com,88`,
  DATA_TRANSFORM: `{
  "operation": "slugify",
  "text": "Hello FlowForge Jobs!"
}`,
  HASH_GENERATE: `{
  "text": "password-to-hash",
  "algorithm": "SHA-256"
}`,
  BASE64_CODEC: `{
  "operation": "encode",
  "text": "Hello FlowForge"
}`,
};

const HELPERS = {
  PYTHON_SCRIPT: 'Paste Python 3 code. stdout becomes the job result. Timeout: 30s.',
  HTTP_REQUEST: 'JSON with url, method (GET/POST/HEAD), optional body. Public URLs only.',
  JSON_FORMAT: 'Paste raw JSON or {"json": "..."}. Validates and pretty-prints.',
  CSV_ANALYZE: 'Paste CSV text. Returns row/column stats and fill rates.',
  DATA_TRANSFORM: 'JSON with operation + text. Ops: uppercase, lowercase, trim, reverse, slugify, normalize_whitespace, sort_lines, dedupe_lines, replace, extract_field.',
  HASH_GENERATE: 'JSON with text + algorithm (SHA-256/512). Optional mode: "lines" or expected hash verification.',
  BASE64_CODEC: 'JSON: operation (encode/decode) and text.',
};

const MULTILINE_TYPES = new Set(['PYTHON_SCRIPT', 'JSON_FORMAT', 'CSV_ANALYZE', 'DATA_TRANSFORM', 'HASH_GENERATE', 'BASE64_CODEC', 'HTTP_REQUEST']);

export default function SubmitJobPage() {
  const [type, setType] = useState('PYTHON_SCRIPT');
  const [payload, setPayload] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleTypeChange = (newType) => {
    setType(newType);
    setPayload(EXAMPLES[newType] || '');
  };

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
          <TextField
            fullWidth
            label="Payload"
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            margin="normal"
            multiline
            rows={MULTILINE_TYPES.has(type) ? 10 : 6}
            required
            placeholder={EXAMPLES[type]}
            helperText={HELPERS[type]}
            sx={{ '& textarea': { fontFamily: 'monospace', fontSize: 13 } }}
          />
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Job'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
