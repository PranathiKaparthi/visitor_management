import React, { useState } from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  Legend,
  Cell
} from 'recharts';

// Pre-seeded professional mock visitors matching the VMS journey background
const INITIAL_VISITORS = [
  {
    id: 'V-2026-001',
    name: 'Pushpa J.',
    company: 'ABC Technologies',
    designation: 'Senior Manager',
    purpose: 'Official Meeting',
    hostEmail: 'host@company.com',
    createdAt: Date.now() - 3600000 * 2, // 2 hours ago (Today, e.g. June 23)
    status: 'PENDING',
    photo: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=150&auto=format&fit=crop', // Realistic corporate portrait
  },
  {
    id: 'V-2026-002',
    name: 'Suresh Kumar',
    company: 'Apex Solutions',
    designation: 'Lead Engineer',
    purpose: 'Maintenance',
    hostEmail: 'it.support@company.com',
    createdAt: Date.now() - 3600000 * 4, // 4 hours ago (Today)
    status: 'PENDING',
    photo: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-003',
    name: 'Emily Smith',
    company: 'Capital Talent LLC',
    designation: 'Recruiting Specialist',
    purpose: 'Interview',
    hostEmail: 'hr@company.com',
    createdAt: Date.now() - 3600000 * 14, // 14 hours ago (Today, e.g. June 23, 10:00 AM)
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-004',
    name: 'Marcus Brody',
    company: 'Swift Logistics PLC',
    designation: 'Delivery Captain',
    purpose: 'Delivery',
    hostEmail: 'security@company.com',
    createdAt: Date.now() - 3600000 * 15, // Today, 9:00 AM
    status: 'DENIED',
    rejectReason: 'Incomplete paperwork & incorrect delivery warehouse address',
    photo: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-005',
    name: 'Anjali Sharma',
    company: 'Fintech Partners',
    designation: 'Advisor',
    purpose: 'Official Meeting',
    hostEmail: 'host@company.com',
    createdAt: Date.now() - 24 * 3600000 * 1 - 3600000 * 14, // Yesterday (June 22), 10:00 AM
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-006',
    name: 'Robert Chen',
    company: 'Cyberdyne Systems',
    designation: 'Auditor',
    purpose: 'Vendor Visit',
    hostEmail: 'compliance@company.com',
    createdAt: Date.now() - 24 * 3600000 * 1 - 3600000 * 10, // Yesterday (June 22), 2:00 PM
    status: 'CHECKED_OUT',
    actualCheckOutTime: Date.now() - 24 * 3600000 * 1 - 3600000 * 8,
    photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-007',
    name: 'Meena Patel',
    company: 'Nexus Consultants',
    designation: 'HR Executive',
    purpose: 'Interview',
    hostEmail: 'hr@company.com',
    createdAt: Date.now() - 24 * 3600000 * 1 - 3600000 * 13, // Yesterday, 11:00 AM
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-008',
    name: 'John Miller',
    company: 'Security Safe Ltd',
    designation: 'Technician',
    purpose: 'Maintenance',
    hostEmail: 'facility@company.com',
    createdAt: Date.now() - 24 * 3600000 * 2 - 3600000 * 16, // 2 days ago (June 21), 8:00 AM
    status: 'CHECKED_OUT',
    actualCheckOutTime: Date.now() - 24 * 3600000 * 2 - 3600000 * 12,
    photo: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-009',
    name: 'Sarah Connor',
    company: 'Resistance Org',
    designation: 'Strategic Analyst',
    purpose: 'Official Meeting',
    hostEmail: 'executive@company.com',
    createdAt: Date.now() - 24 * 3600000 * 2 - 3600000 * 11, // 2 days ago, 1:00 PM
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-010',
    name: 'Vikram Seth',
    company: 'Titan Industries',
    designation: 'Regional Director',
    purpose: 'Official Meeting',
    hostEmail: 'host@company.com',
    createdAt: Date.now() - 24 * 3600000 * 3 - 3600000 * 11, // 3 days ago (June 20), 1:00 PM
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-011',
    name: 'David G.',
    company: 'Global Delivery Inc',
    designation: 'Courier Lead',
    purpose: 'Delivery',
    hostEmail: 'reception@company.com',
    createdAt: Date.now() - 24 * 3600000 * 3 - 3600000 * 13, // 3 days ago, 11:00 AM
    status: 'CHECKED_OUT',
    actualCheckOutTime: Date.now() - 24 * 3600000 * 3 - 3600000 * 12,
    photo: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=150&auto=format&fit=crop',
  },
  {
    id: 'V-2026-012',
    name: 'Clara Oswald',
    company: 'BBC Education',
    designation: 'Researcher',
    purpose: 'Guest Visit',
    hostEmail: 'host@company.com',
    createdAt: Date.now() - 24 * 3600000 * 4 - 3600000 * 10, // 4 days ago (June 19), 2:00 PM
    status: 'APPROVED',
    photo: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=150&auto=format&fit=crop',
  }
];

const HostDashboard = ({ onStatusChange }) => {
  const [visitors, setVisitors] = useState(INITIAL_VISITORS);
  const [activeTab, setActiveTab] = useState('PENDING'); // PENDING, APPROVED, CHECKED_OUT, DENIED, ALL
  const [searchQuery, setSearchQuery] = useState('');
  const [denialModalId, setDenialModalId] = useState(null);
  const [tempRejectReason, setTempRejectReason] = useState('');
  const [showAnalytics, setShowAnalytics] = useState(true);

  // Calculate Daily Visitor Volume dynamically
  const getDailyVolumeData = () => {
    const dailyMap = {};
    
    // Seed the last 5 days including today so we don't present gaps
    for (let i = 4; i >= 0; i--) {
      const d = new Date(Date.now() - i * 24 * 3600000);
      const label = d.toLocaleDateString([], { month: 'short', day: 'numeric' });
      dailyMap[label] = { day: label, Volume: 0, Approved: 0, Denied: 0, Pending: 0 };
    }
    
    visitors.forEach((v) => {
      const dateLabel = new Date(v.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric' });
      if (!dailyMap[dateLabel]) {
        dailyMap[dateLabel] = { day: dateLabel, Volume: 0, Approved: 0, Denied: 0, Pending: 0 };
      }
      dailyMap[dateLabel].Volume += 1;
      if (v.status === 'APPROVED' || v.status === 'CHECKED_OUT') {
        dailyMap[dateLabel].Approved += 1;
      } else if (v.status === 'DENIED') {
        dailyMap[dateLabel].Denied += 1;
      } else if (v.status === 'PENDING') {
        dailyMap[dateLabel].Pending += 1;
      }
    });

    // Sort chronologically using Date instances
    return Object.values(dailyMap).sort((a, b) => {
      const year = new Date().getFullYear();
      return new Date(`${a.day}, ${year}`) - new Date(`${b.day}, ${year}`);
    });
  };

  // Calculate Peak Arrival Hours dynamically
  const getHourlyPeakData = () => {
    const workingHours = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18];
    const hourMap = {};
    
    workingHours.forEach(h => {
      const formatted = `${String(h).padStart(2, '0')}:00`;
      hourMap[h] = { hourNum: h, hour: formatted, Visitors: 0 };
    });

    visitors.forEach(v => {
      const hour = new Date(v.createdAt).getHours();
      if (hourMap[hour] !== undefined) {
        hourMap[hour].Visitors += 1;
      } else if (hour >= 6 && hour < 22) {
        // Outside normal standard but close, show dynamically
        const label = `${String(hour).padStart(2, '0')}:00`;
        hourMap[hour] = { hourNum: hour, hour: label, Visitors: 1 };
      }
    });

    return Object.values(hourMap).sort((a, b) => a.hourNum - b.hourNum);
  };

  const dailyVolumeData = getDailyVolumeData();
  const hourlyPeakData = getHourlyPeakData();

  // Update Visitor Status Hook
  const handleUpdateStatus = (id, newStatus, reason = '') => {
    setVisitors((prev) =>
      prev.map((v) => {
        if (v.id === id) {
          const updated = { ...v, status: newStatus };
          if (newStatus === 'DENIED' && reason) {
            updated.rejectReason = reason;
          }
          return updated;
        }
        return v;
      })
    );

    if (onStatusChange) {
      const match = visitors.find((v) => v.id === id);
      onStatusChange({ ...match, status: newStatus, rejectReason: reason });
    }
  };

  // Dedicated Check-out Hook for manual checkout action
  const handleCheckOut = (id) => {
    const checkOutTimestamp = Date.now();
    setVisitors((prev) =>
      prev.map((v) => {
        if (v.id === id) {
          return { ...v, status: 'CHECKED_OUT', actualCheckOutTime: checkOutTimestamp };
        }
        return v;
      })
    );

    if (onStatusChange) {
      const match = visitors.find((v) => v.id === id);
      onStatusChange({ ...match, status: 'CHECKED_OUT', actualCheckOutTime: checkOutTimestamp });
    }
  };

  // Open structured dialog modal to handle Denial input safely
  const promptDenial = (id) => {
    setDenialModalId(id);
    setTempRejectReason('');
  };

  const confirmDenial = () => {
    if (!tempRejectReason.trim()) return;
    handleUpdateStatus(denialModalId, 'DENIED', tempRejectReason.trim());
    setDenialModalId(null);
  };

  // Derived counts for metrics cards
  const totalCount = visitors.length;
  const pendingCount = visitors.filter((v) => v.status === 'PENDING').length;
  const approvedCount = visitors.filter((v) => v.status === 'APPROVED').length;
  const checkedOutCount = visitors.filter((v) => v.status === 'CHECKED_OUT').length;
  const deniedCount = visitors.filter((v) => v.status === 'DENIED').length;

  // Filter visitors based on active selection tab + matching name search queries
  const processedVisitors = visitors.filter((visitor) => {
    const matchesTab = activeTab === 'ALL' || visitor.status === activeTab;
    const matchesSearch = 
      visitor.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      visitor.company.toLowerCase().includes(searchQuery.toLowerCase()) ||
      visitor.hostEmail.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTab && matchesSearch;
  });

  // Export current processed log context into clean standard compliant CSV template
  const exportToCSV = () => {
    const headers = ['Visitor ID', 'Name', 'Company', 'Designation', 'Purpose', 'Host Email', 'Registered At', 'Status', 'Check-Out Time', 'Reject Reason'];
    const rows = processedVisitors.map(v => [
      v.id,
      v.name,
      v.company || '',
      v.designation || '',
      v.purpose,
      v.hostEmail,
      new Date(v.createdAt).toLocaleString(),
      v.status,
      v.actualCheckOutTime ? new Date(v.actualCheckOutTime).toLocaleString() : '',
      v.rejectReason || ''
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => 
        row.map(val => {
          const escaped = String(val).replace(/"/g, '""');
          return escaped.includes(',') || escaped.includes('\n') || escaped.includes('"') 
            ? `"${escaped}"` 
            : escaped;
        }).join(',')
      )
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `Visitor_Attendance_Log_${new Date().toISOString().slice(0, 10)}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div style={styles.dashboardContainer}>
      <header style={styles.header}>
        <div>
          <h1 style={styles.headerTitle}>Host Approval Portal</h1>
          <p style={styles.headerSubtitle}>
            Review registration requests, verify credentials, and grant premises gate pass credentials.
          </p>
        </div>
        <div style={styles.syncIndicator}>
          <span style={styles.activeDot}></span>
          <span>Live Sync Enabled</span>
        </div>
      </header>

      {/* Metrics Row Section */}
      <section style={styles.metricsGrid}>
        <div 
          style={{ ...styles.metricCard, borderLeft: '4px solid #1a237e' }}
          onClick={() => setActiveTab('ALL')}
        >
          <div style={styles.metricHeader}>
            <span>All Registrations</span>
            <span style={styles.metricIcon}>📋</span>
          </div>
          <p style={styles.metricValue}>{totalCount}</p>
        </div>

        <div 
          style={{ ...styles.metricCard, borderLeft: '4px solid #f97316' }}
          onClick={() => setActiveTab('PENDING')}
        >
          <div style={styles.metricHeader}>
            <span>Pending Approvals</span>
            <span style={styles.metricIcon}>⏳</span>
          </div>
          <p style={{ ...styles.metricValue, color: '#f97316' }}>{pendingCount}</p>
        </div>

        <div 
          style={{ ...styles.metricCard, borderLeft: '4px solid #16a34a' }}
          onClick={() => setActiveTab('APPROVED')}
        >
          <div style={styles.metricHeader}>
            <span>Approved Passes</span>
            <span style={styles.metricIcon}>✓</span>
          </div>
          <p style={{ ...styles.metricValue, color: '#16a34a' }}>{approvedCount}</p>
        </div>

        <div 
          style={{ ...styles.metricCard, borderLeft: '4px solid #0284c7' }}
          onClick={() => setActiveTab('CHECKED_OUT')}
        >
          <div style={styles.metricHeader}>
            <span>Checked Out</span>
            <span style={styles.metricIcon}>🚪</span>
          </div>
          <p style={{ ...styles.metricValue, color: '#0284c7' }}>{checkedOutCount}</p>
        </div>

        <div 
          style={{ ...styles.metricCard, borderLeft: '4px solid #ef4444' }}
          onClick={() => setActiveTab('DENIED')}
        >
          <div style={styles.metricHeader}>
            <span>Denied Access</span>
            <span style={styles.metricIcon}>✕</span>
          </div>
          <p style={{ ...styles.metricValue, color: '#ef4444' }}>{deniedCount}</p>
        </div>
      </section>

      {/* Structured Denial Dialog Backdrop */}
      {denialModalId && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h3 style={styles.modalTitle}>Refuse Visitor Entrance</h3>
            <p style={styles.modalPrompt}>
              Please supply a valid compliance or spatial reason for rejecting this visitation request.
            </p>
            <textarea
              style={styles.modalTextarea}
              placeholder="e.g. Schedule conflict. Request visitor to reschedule for tomorrow."
              value={tempRejectReason}
              onChange={(e) => setTempRejectReason(e.target.value)}
              rows={3}
              required
            />
            <div style={styles.modalActions}>
              <button 
                onClick={() => setDenialModalId(null)} 
                style={styles.cancelLinkBtn}
              >
                Cancel
              </button>
              <button 
                onClick={confirmDenial} 
                disabled={!tempRejectReason.trim()}
                style={{
                  ...styles.confirmDenialBtn,
                  opacity: tempRejectReason.trim() ? 1 : 0.6,
                  cursor: tempRejectReason.trim() ? 'pointer' : 'not-allowed',
                }}
              >
                Confirm Denial
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Dynamic Statistics Analytics Insights Panel */}
      {showAnalytics && (
        <section style={styles.analyticsSection}>
          <div style={styles.chartCard}>
            <div style={styles.chartHeader}>
              <h3 style={styles.chartTitle}>Daily Visitor Volume</h3>
              <span style={styles.insightsSubtext}>Volume categorized by status & entry day</span>
            </div>
            <div style={{ width: '100%', height: 260 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={dailyVolumeData}
                  margin={{ top: 20, right: 10, left: -20, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis 
                    dataKey="day" 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#64748b', fontSize: 12 }} 
                  />
                  <YAxis 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#64748b', fontSize: 12 }}
                    allowDecimals={false}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#ffffff',
                      border: '1px solid #e2e8f0',
                      borderRadius: '8px',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.08)'
                    }}
                    labelStyle={{ fontWeight: 'bold', color: '#1e293b' }}
                  />
                  <Legend verticalAlign="top" height={36} iconType="circle" />
                  <Bar dataKey="Approved" name="Approved" fill="#16a34a" stackId="status" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Pending" name="Pending" fill="#f97316" stackId="status" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Denied" name="Denied" fill="#ef4444" stackId="status" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div style={styles.chartCard}>
            <div style={styles.chartHeader}>
              <h3 style={styles.chartTitle}>Peak Arrival Hours</h3>
              <span style={styles.insightsSubtext}>Hourly visitor check-in density trends</span>
            </div>
            <div style={{ width: '100%', height: 260 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={hourlyPeakData}
                  margin={{ top: 20, right: 10, left: -20, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis 
                    dataKey="hour" 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#64748b', fontSize: 11 }} 
                  />
                  <YAxis 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#64748b', fontSize: 12 }}
                    allowDecimals={false}
                  />
                  <Tooltip
                    cursor={{ fill: '#f1f5f9', opacity: 0.5 }}
                    contentStyle={{
                      backgroundColor: '#1e293b',
                      border: 'none',
                      borderRadius: '8px',
                      color: '#ffffff',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
                    }}
                    itemStyle={{ color: '#ffffff' }}
                    labelStyle={{ fontWeight: 'bold', color: '#94a3b8' }}
                  />
                  <Bar dataKey="Visitors" fill="#1a237e" radius={[4, 4, 0, 0]}>
                    {
                      hourlyPeakData.map((entry, index) => {
                        // Highlight target peak shift hours dynamically
                        const isPeak = entry.hourNum === 9 || entry.hourNum === 10 || entry.hourNum === 11 || entry.hourNum === 13 || entry.hourNum === 14;
                        return (
                          <Cell 
                            key={`cell-${index}`} 
                            fill={isPeak ? '#2563eb' : '#1a237e'} 
                          />
                        );
                      })
                    }
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </section>
      )}

      {/* Filters and Search Action bar */}
      <div style={styles.ctrlBar}>
        <div style={styles.tabsGroup}>
          {[
            { id: 'PENDING', label: 'Awaiting Action' },
            { id: 'APPROVED', label: 'Active (Approved)' },
            { id: 'CHECKED_OUT', label: 'Checked Out' },
            { id: 'DENIED', label: 'Denied' },
            { id: 'ALL', label: 'All Logs' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                ...styles.tabBtn,
                color: activeTab === tab.id ? '#1a237e' : '#64748b',
                borderBottomColor: activeTab === tab.id ? '#1a237e' : 'transparent',
                fontWeight: activeTab === tab.id ? '700' : '500',
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div style={styles.actionsGroup}>
          <div style={styles.searchWrapper}>
            <span style={styles.searchIcon}>🔍</span>
            <input
              type="text"
              placeholder="Search visitor, company, host..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={styles.searchInput}
            />
          </div>
          <button 
            onClick={() => setShowAnalytics(!showAnalytics)} 
            style={{
              ...styles.insightBtn,
              backgroundColor: showAnalytics ? '#e0f2fe' : '#ffffff',
              border: '1px solid #0284c7',
              color: '#0284c7',
            }}
            title={showAnalytics ? "Hide dynamic analytics insights panel" : "Show dynamic analytics insights panel"}
          >
            <span>📊</span> {showAnalytics ? "Hide Insights" : "Show Insights"}
          </button>
          <button 
            onClick={exportToCSV} 
            style={styles.exportBtn} 
            title="Download modern compliance CSV report of shown visitors"
          >
            <span>📥</span> Export CSV
          </button>
        </div>
      </div>

      {/* Grid List of processed entries */}
      <main style={styles.visitorListContainer}>
        {processedVisitors.length === 0 ? (
          <div style={styles.emptyStateBox}>
            <div style={styles.emptyIcon}>🛡️</div>
            <h3 style={{ margin: '0 0 8px 0', color: '#1e293b' }}>Lobby is clear</h3>
            <p style={{ margin: 0, color: '#64748b', fontSize: '14px' }}>
              No visitation records match the filters or search requirements.
            </p>
          </div>
        ) : (
          <div style={styles.visitorGridMatches}>
            {processedVisitors.map((visitor) => (
              <div key={visitor.id} style={styles.visitorCard}>
                <div style={styles.cardHeader}>
                  <span style={styles.visitorCodeId}>{visitor.id}</span>
                  <span 
                    style={{
                      ...styles.statusBadge,
                      backgroundColor: 
                        visitor.status === 'APPROVED' ? '#dcfce7' :
                        visitor.status === 'CHECKED_OUT' ? '#e0f2fe' :
                        visitor.status === 'DENIED' ? '#fee2e2' : '#ffedd5',
                      color:
                        visitor.status === 'APPROVED' ? '#16a34a' :
                        visitor.status === 'CHECKED_OUT' ? '#0284c7' :
                        visitor.status === 'DENIED' ? '#ef4444' : '#d97706',
                    }}
                  >
                    {visitor.status === 'CHECKED_OUT' ? 'Checked Out' : visitor.status}
                  </span>
                </div>

                <div style={styles.identityGroup}>
                  {visitor.photo ? (
                    <img 
                      src={visitor.photo} 
                      alt={visitor.name} 
                      style={styles.visitorPhotoSquare} 
                      onError={(e) => {
                        // Fallback placeholder if custom base64 or source fails
                        e.target.style.display = 'none';
                        e.target.nextSibling.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div 
                    style={{
                      ...styles.avatarPlaceholderCircle,
                      display: visitor.photo ? 'none' : 'flex',
                    }}
                  >
                    {visitor.name.charAt(0)}
                  </div>

                  <div style={styles.namesBlock}>
                    <h3 style={styles.visitorDisplayName}>{visitor.name}</h3>
                    <p style={styles.visitorCompanyMeta}>
                      {visitor.designation} • <strong>{visitor.company}</strong>
                    </p>
                  </div>
                </div>

                {/* Visitation request specs parameters matching requirements */}
                <div style={styles.visitDetailsBlock}>
                  <div style={styles.detailRow}>
                    <span style={styles.detailLabel}>Purpose:</span>
                    <span style={styles.detailValue}>{visitor.purpose}</span>
                  </div>
                  <div style={styles.detailRow}>
                    <span style={styles.detailLabel}>Host Employee:</span>
                    <span style={styles.detailValue}>{visitor.hostEmail}</span>
                  </div>
                  <div style={styles.detailRow}>
                    <span style={styles.detailLabel}>Registered At:</span>
                    <span style={styles.detailValue}>
                      {new Date(visitor.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  {visitor.actualCheckOutTime && (
                    <div style={styles.detailRow}>
                      <span style={styles.detailLabel}>Checked-Out At:</span>
                      <span style={{ ...styles.detailValue, color: '#0284c7' }}>
                        {new Date(visitor.actualCheckOutTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  )}
                </div>

                {/* If Denied, output structured reason banner */}
                {visitor.rejectReason && (
                  <div style={styles.denialReasonNotification}>
                    <strong>Declined reason: </strong>
                    <span>{visitor.rejectReason}</span>
                  </div>
                )}

                {/* Card CTA Action Footer Controls */}
                {visitor.status === 'PENDING' && (
                  <div style={styles.visualControlsRow}>
                    <button 
                      onClick={() => promptDenial(visitor.id)} 
                      style={styles.denialActionBtn}
                    >
                      <span>✕</span> Deny Entrance
                    </button>
                    <button 
                      onClick={() => handleUpdateStatus(visitor.id, 'APPROVED')} 
                      style={styles.approvalActionBtn}
                    >
                      <span>✓</span> Approve Visitor
                    </button>
                  </div>
                )}

                {visitor.status === 'APPROVED' && (
                  <div style={styles.visualControlsRow}>
                    <button 
                      onClick={() => handleCheckOut(visitor.id)} 
                      style={styles.checkoutActionBtn}
                      title="Manually checkout the visitor and end their active visit session"
                    >
                      <span>🚪</span> Check Out (End Visit)
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

// Polished styling spec details matching standard theme configurations
const styles = {
  dashboardContainer: {
    padding: '32px',
    backgroundColor: '#f8fafc',
    minHeight: '100%',
    fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    boxSizing: 'border-box',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    marginBottom: '28px',
  },
  headerTitle: {
    margin: '0 0 6px 0',
    fontSize: '26px',
    fontWeight: '800',
    color: '#1e293b',
  },
  headerSubtitle: {
    margin: 0,
    fontSize: '14px',
    color: '#64748b',
  },
  syncIndicator: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '12px',
    fontWeight: '600',
    color: '#059669',
    backgroundColor: '#ecfdf5',
    padding: '6px 12px',
    borderRadius: '16px',
    border: '1px solid #a7f3d0',
  },
  activeDot: {
    width: '8px',
    height: '8px',
    borderRadius: '50%',
    backgroundColor: '#10b981',
    animation: 'pulse 1.5s infinite',
  },
  metricsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
    gap: '20px',
    marginBottom: '32px',
  },
  metricCard: {
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    padding: '20px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
    border: '1px solid #e2e8f0',
    cursor: 'pointer',
    transition: 'transform 0.15s ease-in-out, box-shadow 0.15s ease-in-out',
  },
  metricHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '13px',
    fontWeight: '600',
    color: '#64748b',
    marginBottom: '12px',
  },
  metricIcon: {
    fontSize: '18px',
  },
  metricValue: {
    margin: 0,
    fontSize: '32px',
    fontWeight: '800',
    color: '#1e293b',
  },
  ctrlBar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '16px',
    marginBottom: '24px',
    borderBottom: '1px solid #e2e8f0',
    paddingBottom: '8px',
  },
  tabsGroup: {
    display: 'flex',
    gap: '24px',
  },
  tabBtn: {
    background: 'none',
    border: 'none',
    borderBottom: '2px solid transparent',
    padding: '8px 0',
    fontSize: '15px',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
  },
  searchWrapper: {
    display: 'flex',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    border: '1px solid #cbd5e1',
    borderRadius: '8px',
    padding: '8px 14px',
    width: '100%',
    maxWidth: '300px',
  },
  searchIcon: {
    color: '#64748b',
    marginRight: '8px',
    fontSize: '14px',
  },
  searchInput: {
    border: 'none',
    outline: 'none',
    fontSize: '14px',
    width: '100%',
    color: '#334155',
  },
  actionsGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    flexWrap: 'wrap',
  },
  exportBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    backgroundColor: '#ffffff',
    border: '1px solid #1a237e',
    color: '#1a237e',
    borderRadius: '8px',
    padding: '8px 14px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.15s ease-in-out',
  },
  analyticsSection: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
    gap: '24px',
    marginBottom: '32px',
  },
  chartCard: {
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    padding: '24px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
    border: '1px solid #e2e8f0',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  chartHeader: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },
  chartTitle: {
    margin: 0,
    fontSize: '16px',
    fontWeight: '700',
    color: '#1e293b',
  },
  insightsSubtext: {
    fontSize: '12px',
    color: '#64748b',
  },
  insightBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    borderRadius: '8px',
    padding: '8px 14px',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.15s ease-in-out',
  },
  visitorListContainer: {
    width: '100%',
  },
  emptyStateBox: {
    textAlign: 'center',
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    padding: '48px 24px',
    border: '1px solid #e2e8f0',
    boxShadow: '0 1px 3px rgba(0,0,0,0.02)',
  },
  emptyIcon: {
    fontSize: '36px',
    marginBottom: '16px',
  },
  visitorGridMatches: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))',
    gap: '24px',
  },
  visitorCard: {
    backgroundColor: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: '14px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.03)',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    transition: 'box-shadow 0.2s ease',
    boxSizing: 'border-box',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '16px',
  },
  visitorCodeId: {
    fontSize: '12px',
    fontWeight: '700',
    fontFamily: 'monospace',
    color: '#64748b',
    backgroundColor: '#f1f5f9',
    padding: '4px 8px',
    borderRadius: '4px',
  },
  statusBadge: {
    fontSize: '11px',
    fontWeight: '700',
    textTransform: 'uppercase',
    padding: '4px 10px',
    borderRadius: '12px',
    letterSpacing: '0.5px',
  },
  identityGroup: {
    display: 'flex',
    gap: '14px',
    alignItems: 'center',
    marginBottom: '18px',
  },
  visitorPhotoSquare: {
    width: '56px',
    height: '56px',
    borderRadius: '8px',
    objectFit: 'cover',
    border: '1px solid #cbd5e1',
  },
  avatarPlaceholderCircle: {
    width: '56px',
    height: '56px',
    borderRadius: '50%',
    backgroundColor: '#1a237e',
    color: '#ffffff',
    fontSize: '20px',
    fontWeight: 'bold',
    justifyContent: 'center',
    alignItems: 'center',
  },
  namesBlock: {
    display: 'flex',
    flexDirection: 'column',
  },
  visitorDisplayName: {
    margin: '0 0 2px 0',
    fontSize: '17px',
    fontWeight: '700',
    color: '#1e293b',
  },
  visitorCompanyMeta: {
    margin: 0,
    fontSize: '13px',
    color: '#64748b',
    lineHeight: '1.4',
  },
  visitDetailsBlock: {
    backgroundColor: '#f8fafc',
    padding: '12px 14px',
    borderRadius: '8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
    marginBottom: '16px',
    border: '1px solid #f1f5f9',
  },
  detailRow: {
    display: 'flex',
    justifyContent: 'space-between',
    fontSize: '13px',
  },
  detailLabel: {
    color: '#64748b',
    fontWeight: '500',
  },
  detailValue: {
    color: '#334155',
    fontWeight: '600',
  },
  denialReasonNotification: {
    padding: '10px 12px',
    borderRadius: '6px',
    backgroundColor: '#fef2f2',
    border: '1px solid #fee2e2',
    color: '#b91c1c',
    fontSize: '12px',
    lineHeight: '1.4',
    marginBottom: '16px',
  },
  visualControlsRow: {
    display: 'flex',
    gap: '12px',
    marginTop: 'auto',
  },
  denialActionBtn: {
    flex: 1,
    backgroundColor: '#ffffff',
    color: '#dc2626',
    border: '1px solid #fca5a5',
    padding: '10px',
    fontSize: '13px',
    fontWeight: '600',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '6px',
  },
  approvalActionBtn: {
    flex: 1,
    backgroundColor: '#16a34a',
    color: '#ffffff',
    border: 'none',
    padding: '10px',
    fontSize: '13px',
    fontWeight: '600',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'background-color 0.15s ease',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '6px',
  },
  checkoutActionBtn: {
    width: '100%',
    backgroundColor: '#ffffff',
    color: '#0284c7',
    border: '1px solid #bae6fd',
    padding: '10px',
    fontSize: '13px',
    fontWeight: '600',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'all 0.15s ease-in-out',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '6px',
    marginTop: 'auto',
  },

  /* Structured Reason Denial Dialog Box Overlay */
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(15, 23, 42, 0.6)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 999,
  },
  modalContent: {
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    padding: '24px',
    width: '100%',
    maxWidth: '420px',
    boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
    boxSizing: 'border-box',
  },
  modalTitle: {
    margin: '0 0 8px 0',
    fontSize: '18px',
    fontWeight: '700',
    color: '#1e293b',
  },
  modalPrompt: {
    margin: '0 0 16px 0',
    fontSize: '13px',
    color: '#64748b',
    lineHeight: '1.4',
  },
  modalTextarea: {
    width: '100%',
    padding: '10px',
    fontSize: '14px',
    color: '#334155',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    boxSizing: 'border-box',
    fontFamily: 'inherit',
    outline: 'none',
    resize: 'none',
  },
  modalActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '12px',
    marginTop: '20px',
  },
  cancelLinkBtn: {
    background: 'none',
    border: 'none',
    color: '#64748b',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    padding: '8px 16px',
  },
  confirmDenialBtn: {
    backgroundColor: '#ef4444',
    color: '#ffffff',
    border: 'none',
    borderRadius: '6px',
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: '600',
  },
};

export default HostDashboard;
