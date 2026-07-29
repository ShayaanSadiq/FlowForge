import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  ListSubheader,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { jobsApi } from '../api/client';

const JOB_TYPES = [
  { group: 'Practical', value: 'PYTHON_SCRIPT', label: 'Python Script' },
  { group: 'Practical', value: 'HTTP_REQUEST', label: 'HTTP Request' },
  { group: 'Practical', value: 'JSON_FORMAT', label: 'JSON Format & Validate' },
  { group: 'Practical', value: 'CSV_ANALYZE', label: 'CSV Analyze' },
  { group: 'Practical', value: 'HASH_GENERATE', label: 'Hash Generate (SHA-256/512)' },
  { group: 'Practical', value: 'BASE64_CODEC', label: 'Base64 Encode/Decode' },
  { group: 'Demo', value: 'SIMULATION', label: 'Simulation (demo)' },
  { group: 'Demo', value: 'DATA_TRANSFORM', label: 'Data Transform (demo)' },
  { group: 'Demo', value: 'REPORT_GENERATION', label: 'Report Generation (demo)' },
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
  HASH_GENERATE: `{
  "text": "password-to-hash",
  "algorithm": "SHA-256"
}`,
  BASE64_CODEC: `{
  "operation": "encode",
  "text": "Hello FlowForge"
}`,
  SIMULATION: 'model-training-run-42',
  DATA_TRANSFORM: 'hello flowforge',
  REPORT_GENERATION: 'Q3 sales summary',
};

const HELPERS = {
  PYTHON_SCRIPT: 'Paste Python 3 code. stdout becomes the job result. Timeout: 30s.',
  HTTP_REQUEST: 'JSON with url, method (GET/POST/HEAD), optional body. Public URLs only.',
  JSON_FORMAT: 'Paste raw JSON or {"json": "..."}. Validates and pretty-prints.',
  CSV_ANALYZE: 'Paste CSV text. Returns row/column stats and fill rates.',
  HASH_GENERATE: 'Plain text or JSON with text + algorithm (SHA-256 or SHA-512).',
  BASE64_CODEC: 'JSON: operation (encode/decode) and text.',
  SIMULATION: 'Demo job — fakes multi-step work for testing the platform.',
  DATA_TRANSFORM: 'Demo job — uppercases your text.',
  REPORT_GENERATION: 'Demo job — simulates report generation.',
};

export default function SubmitJobPage() {
  const [type, setType] = useState('PYTHON_SCRIPT');
  const [payload, setPayload] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const isMultiline = type === 'PYTHON_SCRIPT' || type === 'JSON_FORMAT' || type === 'CSV_ANALYZE';
  const isMonospace = type !== 'SIMULATION' && type !== 'DATA_TRANSFORM' && type !== 'REPORT_GENERATION';

  const groupedOptions = useMemo(() => {
    const groups = {};
    JOB_TYPES.forEach((option) => {
      groups[option.group] = groups[option.group] || [];
      groups[option.group].push(option);
    });
    return groups;
  }, []);

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
            {Object.entries(groupedOptions).flatMap(([group, options]) => [
              <ListSubheader key={group}>{group}</ListSubheader>,
              ...options.map((option) => (
                <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
              )),
            ])}
          </TextField>
          <TextField
            fullWidth
            label="Payload"
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            margin="normal"
            multiline
            rows={isMultiline ? 10 : 6}
            required
            placeholder={EXAMPLES[type]}
            helperText={HELPERS[type]}
            sx={isMonospace ? { '& textarea': { fontFamily: 'monospace', fontSize: 13 } } : undefined}
          />
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Job'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
