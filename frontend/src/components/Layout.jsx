import { Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Button,
  Container,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import useIsMobile from '../hooks/useIsMobile';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isMobile = useIsMobile('sm');

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="static"
        elevation={0}
        sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}
      >
        <Toolbar
          sx={{
            flexWrap: 'wrap',
            gap: 1,
            py: { xs: 1, sm: 0 },
            minHeight: { xs: 56, sm: 64 },
          }}
        >
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 700, color: 'primary.main', minWidth: 120 }}
          >
            FlowForge
          </Typography>
          {!isMobile && (
            <Typography variant="body2" sx={{ mr: 1, color: 'text.secondary' }}>
              {user?.displayName}
            </Typography>
          )}
          <Stack
            direction="row"
            spacing={0.5}
            sx={{ width: { xs: '100%', sm: 'auto' }, justifyContent: { xs: 'flex-end', sm: 'flex-start' } }}
          >
            <Button size="small" onClick={() => navigate('/')}>Dashboard</Button>
            <Button size="small" onClick={() => navigate('/jobs/new')}>New Job</Button>
            <Button size="small" color="inherit" onClick={logout}>Logout</Button>
          </Stack>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 2, sm: 4 }, px: { xs: 2, sm: 3 } }}>
        <Outlet />
      </Container>
    </Box>
  );
}
