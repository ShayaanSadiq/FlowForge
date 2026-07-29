import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Pagination,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { jobsApi, statsApi } from '../api/client';
import StatusBadge from '../components/StatusBadge';

const PAGE_SIZE_OPTIONS = [20, 50, 100];

export default function DashboardPage() {
  const [jobs, setJobs] = useState([]);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalJobs, setTotalJobs] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadData = useCallback(async () => {
    try {
      const [jobsPage, statsData] = await Promise.all([
        jobsApi.list(page, pageSize),
        statsApi.get(),
      ]);

      const pages = jobsPage.totalPages ?? 0;
      if (pages > 0 && page >= pages) {
        setPage(pages - 1);
        return;
      }

      setJobs(jobsPage.content || []);
      setTotalJobs(jobsPage.totalElements ?? 0);
      setTotalPages(pages);
      setStats(statsData);
      setError('');
    } catch (err) {
      setError(err.message);
    }
  }, [page, pageSize]);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, [loadData]);

  const handlePageSizeChange = (event) => {
    setPageSize(event.target.value);
    setPage(0);
  };

  const handlePageChange = (_event, value) => {
    setPage(value - 1);
  };

  const rangeStart = totalJobs === 0 ? 0 : page * pageSize + 1;
  const rangeEnd = Math.min((page + 1) * pageSize, totalJobs);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" fontWeight={700}>Job Dashboard</Typography>
        <Button component={RouterLink} to="/jobs/new" variant="contained">Submit Job</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {stats && Object.entries(stats).map(([key, value]) => (
          <Grid item xs={6} sm={4} md={2.4} key={key}>
            <Card>
              <CardContent>
                <Typography variant="overline" color="text.secondary">{key}</Typography>
                <Typography variant="h5" fontWeight={700}>{value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 2 }}>
        <Typography variant="body2" color="text.secondary">
          {totalJobs === 0
            ? 'No jobs'
            : `Showing ${rangeStart}–${rangeEnd} of ${totalJobs} job${totalJobs !== 1 ? 's' : ''}`}
        </Typography>
        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel id="page-size-label">Jobs per page</InputLabel>
          <Select
            labelId="page-size-label"
            value={pageSize}
            label="Jobs per page"
            onChange={handlePageSizeChange}
          >
            {PAGE_SIZE_OPTIONS.map((size) => (
              <MenuItem key={size} value={size}>{size}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Attempts</TableCell>
              <TableCell>Created</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {jobs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">No jobs yet. Submit your first job.</TableCell>
              </TableRow>
            ) : jobs.map((job) => (
              <TableRow key={job.id} hover>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>{job.id?.slice(-8)}</TableCell>
                <TableCell>{job.type}</TableCell>
                <TableCell><StatusBadge status={job.status} /></TableCell>
                <TableCell>{job.attempts}/{job.maxAttempts}</TableCell>
                <TableCell>{job.createdAt ? new Date(job.createdAt).toLocaleString() : '-'}</TableCell>
                <TableCell>
                  <Button component={RouterLink} to={`/jobs/${job.id}`} size="small">View</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {totalPages > 1 && (
          <Stack direction="row" justifyContent="center" sx={{ py: 2 }}>
            <Pagination
              count={totalPages}
              page={page + 1}
              onChange={handlePageChange}
              color="primary"
              showFirstButton
              showLastButton
            />
          </Stack>
        )}
      </Paper>
    </Box>
  );
}
