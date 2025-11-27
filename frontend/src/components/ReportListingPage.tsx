import React, { useState, useEffect } from 'react';
import { ThemeToggle } from './ThemeToggle';
import { adminApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface Report {
  reportId: number;
  reportType: string;
  description: string;
  listingId: string;
  listing?: any;
  reporter?: any;
  status: string;
  severity: string;
  createdAt: string;
}

const getIconBg = (color: string) => {
  const colors: Record<string, string> = {
    amber: 'bg-amber-500',
    red: 'bg-red-500',
    emerald: 'bg-emerald-500',
    slate: 'bg-slate-500',
  };
  return colors[color] || 'bg-gray-500';
};

const getSeverityStyle = (severity: string) => {
  switch (severity?.toLowerCase()) {
    case 'high': return 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-400';
    case 'medium': return 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-400';
    default: return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400';
  }
};

const getStatusStyle = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-400';
    case 'UNDER_REVIEW': return 'bg-indigo-100 text-indigo-700 dark:bg-indigo-500/20 dark:text-indigo-400';
    case 'RESOLVED': return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400';
    default: return 'bg-slate-200 text-slate-700 dark:bg-slate-600/50 dark:text-slate-300';
  }
};

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function ReportListingPage() {
  const { user, isAdmin: checkIsAdmin } = useAuth();
  const isAdmin = checkIsAdmin();
  
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [severityFilter, setSeverityFilter] = useState('all');
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedReport, setSelectedReport] = useState<Report | null>(null);

  const stats = [
    { label: 'Pending Reports', value: reports.filter(r => r.status === 'PENDING').length, color: 'amber', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    )},
    { label: 'High Priority', value: reports.filter(r => r.severity === 'high').length, color: 'red', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    )},
    { label: 'Resolved', value: reports.filter(r => r.status === 'RESOLVED').length, color: 'emerald', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    )},
    { label: 'Dismissed', value: reports.filter(r => r.status === 'DISMISSED').length, color: 'slate', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
      </svg>
    )},
  ];

  useEffect(() => {
    loadReports();
  }, []);

  async function loadReports() {
    try {
      setLoading(true);
      const data = await adminApi.getReports();
      setReports(data.reports || []);
    } catch (err) {
      console.error('Failed to load reports:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateReport(reportId: number, status: string) {
    try {
      await adminApi.updateReport(reportId, { status });
      loadReports();
    } catch (err) {
      console.error('Failed to update report:', err);
      alert('Failed to update report');
    }
  }

  const filteredReports = reports.filter(report => {
    const matchesSearch = 
      report.listing?.title?.toLowerCase().includes(search.toLowerCase()) || 
      report.description?.toLowerCase().includes(search.toLowerCase()) ||
      report.reporter?.username?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'all' || report.status === statusFilter;
    const matchesSeverity = severityFilter === 'all' || report.severity === severityFilter;
    return matchesSearch && matchesStatus && matchesSeverity;
  });

  return (
    <div className="min-h-screen">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      
      <header className="nav-glass px-4 sm:px-8 py-4 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 sm:gap-4">
            <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
              <svg className="w-4 h-4 sm:w-5 sm:h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-xl font-bold gradient-text">CampusConnect</h1>
              <p className="text-sm text-muted">Report Management</p>
            </div>
            <h1 className="sm:hidden text-lg font-bold gradient-text">Reports</h1>
          </div>
          <div className="flex items-center gap-2 sm:gap-3">
            <ThemeToggle />
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-8 py-6">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          {stats.map(stat => (
            <div key={stat.label} className="glass-card p-4 sm:p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className={`w-9 h-9 rounded-lg ${getIconBg(stat.color)} flex items-center justify-center text-white`}>
                  {stat.icon}
                </div>
                <span className="text-sm text-muted font-medium">{stat.label}</span>
              </div>
              <p className="text-3xl sm:text-4xl font-bold">{stat.value}</p>
            </div>
          ))}
        </div>
        
        <div className="glass-card p-4 mb-6">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Search by listing title, description, or reporter..."
                className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-slate-600 bg-white dark:bg-slate-800 text-gray-900 dark:text-white text-sm placeholder-gray-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            <div className="flex gap-3">
              <div className="relative">
                <select
                  value={statusFilter}
                  onChange={e => setStatusFilter(e.target.value)}
                  className="appearance-none px-4 py-3 pr-10 rounded-xl bg-white dark:bg-slate-800 text-gray-900 dark:text-white text-sm font-medium cursor-pointer min-w-[130px] border border-gray-200 dark:border-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="all">All Status</option>
                  <option value="PENDING">Pending</option>
                  <option value="UNDER_REVIEW">Under Review</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="DISMISSED">Dismissed</option>
                </select>
                <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
              <div className="relative">
                <select
                  value={severityFilter}
                  onChange={e => setSeverityFilter(e.target.value)}
                  className="appearance-none px-4 py-3 pr-10 rounded-xl bg-white dark:bg-slate-800 text-gray-900 dark:text-white text-sm font-medium cursor-pointer min-w-[130px] border border-gray-200 dark:border-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="all">All Severity</option>
                  <option value="low">Low</option>
                  <option value="medium">Medium</option>
                  <option value="high">High</option>
                </select>
                <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
            </div>
          </div>
        </div>
        
        <div className="glass-card overflow-hidden">
          <div className="p-4 sm:p-5 border-b border-white/10 dark:border-white/5">
            <h3 className="font-semibold flex items-center gap-2">
              <svg className="w-5 h-5 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Reported Listings
            </h3>
          </div>
          
          {loading ? (
            <div className="p-12 text-center">
              <div className="w-12 h-12 mx-auto mb-4 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin"></div>
              <p className="text-muted">Loading reports...</p>
            </div>
          ) : filteredReports.length === 0 ? (
            <div className="text-center py-12 px-4">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-white/50 dark:bg-white/10 flex items-center justify-center">
                <svg className="w-8 h-8 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <p className="text-muted font-medium">No reports match your filters</p>
            </div>
          ) : (
            <div className="divide-y divide-white/10 dark:divide-white/5">
              {filteredReports.map(report => (
                <div key={report.reportId} className="p-4 sm:p-5 hover:bg-white/30 dark:hover:bg-white/5 transition-colors">
                  <div className="flex flex-col sm:flex-row gap-4">
                    <div className="w-full sm:w-24 h-20 sm:h-24 rounded-xl overflow-hidden flex-shrink-0 bg-slate-200 dark:bg-slate-700">
                      {report.listing?.imageUrl ? (
                        <img src={report.listing.imageUrl} alt={report.listing?.title} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center">
                          <svg className="w-8 h-8 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                        </div>
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex flex-wrap items-center gap-2 mb-1">
                        <h4 className="font-semibold">{report.listing?.title || `Listing #${report.listingId}`}</h4>
                        <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${getSeverityStyle(report.severity)}`}>
                          {report.severity}
                        </span>
                        <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${getStatusStyle(report.status)}`}>
                          {report.status?.replace(/_/g, ' ')}
                        </span>
                      </div>
                      
                      <p className="text-xs text-muted mb-1">Report ID: RPT-{String(report.reportId).padStart(3, '0')}</p>
                      <p className="text-xs text-muted mb-2">Listing ID: {report.listingId}</p>
                      
                      <p className="text-sm mb-3 flex items-center gap-2">
                        <svg className="w-4 h-4 text-amber-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        {report.description}
                      </p>
                      
                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted mb-4">
                        <span className="flex items-center gap-1">
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                          Reporter: {report.reporter?.username || report.reporter?.email || 'Unknown'}
                        </span>
                        <span className="flex items-center gap-1">
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                          Seller: {report.listing?.seller?.username || report.listing?.seller?.name || 'Unknown'}
                        </span>
                        <span className="flex items-center gap-1">
                          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                          {formatDate(report.createdAt)}
                        </span>
                      </div>
                      
                      <div className="flex flex-wrap gap-2">
                        {isAdmin && (report.status === 'PENDING' || report.status === 'UNDER_REVIEW') && (
                          <button 
                            onClick={() => setSelectedReport(report)}
                            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium transition-colors"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                            Review
                          </button>
                        )}
                        {!isAdmin && (report.status === 'PENDING' || report.status === 'UNDER_REVIEW') && (
                          <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-amber-100 dark:bg-amber-500/20 text-amber-700 dark:text-amber-400 text-sm font-medium">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            Pending Review
                          </span>
                        )}
                        {report.status === 'RESOLVED' && (
                          <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-emerald-100 dark:bg-emerald-500/20 text-emerald-700 dark:text-emerald-400 text-sm font-medium">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                            Resolved
                          </span>
                        )}
                        {report.status === 'DISMISSED' && (
                          <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-slate-100 dark:bg-slate-600/30 text-slate-600 dark:text-slate-400 text-sm font-medium">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            Dismissed
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      {selectedReport && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div 
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setSelectedReport(null)}
          ></div>
          <div className="relative w-full max-w-2xl glass-card p-0 overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="relative h-48 bg-gradient-to-br from-indigo-500/20 to-purple-500/20">
              {selectedReport.listing?.imageUrl ? (
                <img 
                  src={selectedReport.listing.imageUrl} 
                  alt={selectedReport.listing?.title || 'Listing'} 
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <svg className="w-16 h-16 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              )}
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
              <button
                onClick={() => setSelectedReport(null)}
                className="absolute top-4 right-4 w-8 h-8 rounded-full bg-black/30 backdrop-blur-sm flex items-center justify-center text-white hover:bg-black/50 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
              <div className="absolute bottom-4 left-6 right-6">
                <h3 className="text-xl font-bold text-white mb-1">
                  {selectedReport.listing?.title || `Listing #${selectedReport.listingId}`}
                </h3>
                <div className="flex items-center gap-2">
                  <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${getSeverityStyle(selectedReport.severity)}`}>
                    {selectedReport.severity} severity
                  </span>
                  <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${getStatusStyle(selectedReport.status)}`}>
                    {selectedReport.status?.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>
            </div>
            
            <div className="p-6 space-y-4">
              <div>
                <h4 className="text-sm font-medium text-muted mb-1">Report Type</h4>
                <p className="font-medium">{selectedReport.reportType?.replace(/_/g, ' ')}</p>
              </div>
              
              <div>
                <h4 className="text-sm font-medium text-muted mb-1">Description</h4>
                <p className="text-sm leading-relaxed">{selectedReport.description}</p>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h4 className="text-sm font-medium text-muted mb-1">Reported By</h4>
                  <p className="text-sm flex items-center gap-2">
                    <svg className="w-4 h-4 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    {selectedReport.reporter?.username || selectedReport.reporter?.email || 'Unknown'}
                  </p>
                </div>
                <div>
                  <h4 className="text-sm font-medium text-muted mb-1">Date Reported</h4>
                  <p className="text-sm flex items-center gap-2">
                    <svg className="w-4 h-4 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {formatDate(selectedReport.createdAt)}
                  </p>
                </div>
              </div>

              {selectedReport.listing && (
                <div className="p-4 rounded-xl bg-white/50 dark:bg-white/5">
                  <h4 className="text-sm font-medium text-muted mb-2">Listing Details</h4>
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    {selectedReport.listing.price && (
                      <div>
                        <span className="text-muted">Price:</span>
                        <span className="ml-2 font-medium gradient-text">${selectedReport.listing.price}</span>
                      </div>
                    )}
                    {selectedReport.listing.category && (
                      <div>
                        <span className="text-muted">Category:</span>
                        <span className="ml-2">{selectedReport.listing.category?.replace(/_/g, ' ')}</span>
                      </div>
                    )}
                    {selectedReport.listing.condition && (
                      <div>
                        <span className="text-muted">Condition:</span>
                        <span className="ml-2">{selectedReport.listing.condition?.replace(/_/g, ' ')}</span>
                      </div>
                    )}
                    {selectedReport.listing.seller && (
                      <div>
                        <span className="text-muted">Seller:</span>
                        <span className="ml-2">{selectedReport.listing.seller.username || selectedReport.listing.seller.name}</span>
                      </div>
                    )}
                  </div>
                </div>
              )}
              
              <div className="flex gap-3 pt-4 border-t border-white/10 dark:border-white/5">
                <button
                  onClick={() => {
                    handleUpdateReport(selectedReport.reportId, 'RESOLVED');
                    setSelectedReport(null);
                  }}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-medium transition-all duration-200 shadow-sm hover:shadow-md"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Approve Listing
                </button>
                <button
                  onClick={() => {
                    handleUpdateReport(selectedReport.reportId, 'DISMISSED');
                    setSelectedReport(null);
                  }}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-medium transition-all duration-200 shadow-sm hover:shadow-md"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  Reject Listing
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
