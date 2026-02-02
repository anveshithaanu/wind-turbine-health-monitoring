import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TurbineService } from './services/turbine.service';
import { Turbine, Farm, HealthAlert } from './models/turbine.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterOutlet,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  turbines: Turbine[] = [];
  farms: Farm[] = [];
  alerts: HealthAlert[] = [];
  loading: boolean = false;
  error: string | null = null;
  
  currentPage: string = 'dashboard';
  selectedTabIndex: number = 0;
  
  dashboardFilters = {
    farm: '',
    region: ''
  };
  
  turbinesFilters = {
    farm: '',
    region: '',
    status: ''
  };
  
  analyticsFilters = {
    farm: '',
    region: '',
    period: '30'
  };
  analyticsData: any[] = [];
  graphData: any[] = [];
  maxGeneration: number = 1000;
  minGeneration: number = 0;
  maxEfficiency: number = 100;
  minEfficiency: number = 0;
  avgEfficiency: number = 0; // Store calculated average efficiency
  
  selectedTurbine: Turbine | null = null;
  showDetails: boolean = false;
  
  isDarkTheme: boolean = false;
  
  pagination = {
    currentPage: 0,
    itemsPerPage: 10,
    totalItems: 0,
    totalPages: 0
  };
  
  alertPagination = {
    currentPage: 0,
    itemsPerPage: 10,
    totalItems: 0,
    totalPages: 0
  };
  
  turbinesData: any = null;
  alertsData: any = null;
  
  stats = {
    totalTurbines: 0,
    activeTurbines: 0,
    activeAlerts: 0,
    totalFarms: 0
  };
  
  constructor(private turbineService: TurbineService) {}
  
  
  loadData(): void {
    this.loading = true;
    this.error = null;
    
    this.turbineService.getFarms().subscribe({
      next: (farms) => {
        this.farms = farms;
        this.stats.totalFarms = farms.length;
        if (this.currentPage === 'dashboard' || this.currentPage === 'turbines') {
          this.loadTurbines();
        } else {
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Error loading farms:', err);
        this.farms = [];
        this.loading = false;
        if (err.status === 0 || err.status === 404) {
          this.error = 'Backend not running. Please start the backend server.';
        } else {
          this.error = null;
          if (this.currentPage === 'dashboard' || this.currentPage === 'turbines') {
            this.loadTurbines();
          }
        }
      }
    });
  }
  
  loadTurbines(): void {
    if (this.currentPage !== 'dashboard' && this.currentPage !== 'turbines') {
      return;
    }
    
    this.loading = this.currentPage === 'dashboard';
    
    if (this.currentPage === 'dashboard') {
      this.turbineService.getTurbines(
        this.dashboardFilters.farm, 
        this.dashboardFilters.region, 
        undefined
      ).subscribe({
        next: (response) => {
          if (Array.isArray(response)) {
            this.turbines = response;
            this.turbinesData = null;
            this.stats.totalTurbines = response.length;
            this.stats.activeTurbines = response.filter(t => t.status === 'ACTIVE').length;
            this.loading = false;
            this.error = null;
            this.loadAlerts();
          }
        },
        error: (err) => {
          console.error('Error loading turbines:', err);
          this.turbines = [];
          this.turbinesData = null;
          this.stats.totalTurbines = 0;
          this.loading = false;
          if (err.status === 0 || err.status === 404) {
            this.error = 'Backend not running. Please start the backend server.';
          } else {
            this.error = null;
          }
        }
      });
    } else {
      this.turbineService.getTurbines(
        this.turbinesFilters.farm, 
        this.turbinesFilters.region, 
        this.turbinesFilters.status,
        this.pagination.currentPage,
        this.pagination.itemsPerPage
      ).subscribe({
        next: (response) => {
          if (this.isPageResponse(response)) {
            this.turbinesData = response;
            this.turbines = response.content;
            this.pagination.totalItems = response.totalElements;
            this.pagination.totalPages = response.totalPages;
            this.pagination.currentPage = response.page;
          } else {
            this.turbines = response;
            this.turbinesData = null;
            this.pagination.totalItems = response.length;
            this.pagination.totalPages = Math.ceil(response.length / this.pagination.itemsPerPage);
          }
          this.stats.totalTurbines = this.pagination.totalItems;
          this.stats.activeTurbines = this.turbines.filter(t => t.status === 'ACTIVE').length;
          this.loading = false;
          this.error = null;
        },
        error: (err) => {
          console.error('Error loading turbines:', err);
          this.turbines = [];
          this.turbinesData = null;
          this.pagination.totalItems = 0;
          this.pagination.totalPages = 0;
          this.loading = false;
          if (err.status === 0 || err.status === 404) {
            this.error = 'Backend not running. Please start the backend server.';
          } else {
            this.error = null;
          }
        }
      });
    }
  }
  
  isPageResponse(response: any): response is { content: any[], totalElements: number, totalPages: number, page: number } {
    return response && typeof response === 'object' && 'content' in response && 'totalElements' in response;
  }
  
  getPaginatedTurbines(): Turbine[] {
    return this.turbines;
  }
  
  getPaginatedAlerts(): HealthAlert[] {
    return this.alerts;
  }
  
  goToPage(page: number): void {
    if (page >= 0 && page < this.pagination.totalPages) {
      this.pagination.currentPage = page;
      this.loadTurbines();
    }
  }
  
  goToAlertPage(page: number): void {
    if (page >= 0 && page < this.alertPagination.totalPages) {
      this.alertPagination.currentPage = page;
      this.loadAlerts();
    }
  }
  
  changeItemsPerPage(items: number): void {
    if (items > 0 && items <= 100) {
      this.pagination.itemsPerPage = items;
      this.pagination.currentPage = 0;
      this.loadTurbines();
    }
  }
  
  changeAlertItemsPerPage(items: number): void {
    if (items > 0 && items <= 100) {
      this.alertPagination.itemsPerPage = items;
      this.alertPagination.currentPage = 0;
      this.loadAlerts();
    }
  }
  
  loadAlerts(): void {
    if (this.currentPage !== 'dashboard' && this.currentPage !== 'alerts') {
      return;
    }
    
    this.turbineService.getActiveAlerts(
      undefined,
      undefined,
      undefined,
      this.alertPagination.currentPage,
      this.alertPagination.itemsPerPage
    ).subscribe({
      next: (response) => {
        if (this.isPageResponse(response)) {
          this.alertsData = response;
          this.alerts = response.content;
          this.alertPagination.totalItems = response.totalElements;
          this.alertPagination.totalPages = response.totalPages;
          this.alertPagination.currentPage = response.page;
        } else {
          this.alerts = response;
          this.alertsData = null;
          this.alertPagination.totalItems = response.length;
          this.alertPagination.totalPages = Math.ceil(response.length / this.alertPagination.itemsPerPage);
        }
        this.stats.activeAlerts = this.alertPagination.totalItems;
      },
      error: (err) => {
        console.error('Error loading alerts:', err);
        this.alerts = [];
        this.alertsData = null;
        this.alertPagination.totalItems = 0;
        this.alertPagination.totalPages = 0;
      }
    });
  }
  
  applyFilters(): void {
    if (this.currentPage === 'dashboard') {
      this.loadTurbines();
    } else if (this.currentPage === 'turbines') {
      this.pagination.currentPage = 0;
      this.loadTurbines();
    }
  }
  
  viewTurbineDetails(turbine: Turbine): void {
    this.selectedTurbine = turbine;
    this.showDetails = true;
  }
  
  closeDetails(): void {
    this.showDetails = false;
    this.selectedTurbine = null;
  }
  
  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
  
  getTurbineAlerts(turbineId: number): HealthAlert[] {
    return this.alerts.filter(a => a.turbine?.id === turbineId);
  }
  
  getRegions(): string[] {
    return [...new Set(this.farms.map(f => f.region))];
  }
  
  resolveAlert(alertId: number | undefined): void {
    if (!alertId) {
      console.error('Alert ID is missing');
      window.alert('Cannot resolve alert: Alert ID is missing.');
      return;
    }
    
    console.log('Resolving alert with ID:', alertId);
    
    // Check if alert is already resolved
    const healthAlert = this.alerts.find(a => a.id === alertId);
    if (healthAlert && healthAlert.status === 'RESOLVED') {
      console.warn('Alert is already resolved');
      return;
    }
    
    // Optimistically remove the alert from the UI
    const alertIndex = this.alerts.findIndex(a => a.id === alertId);
    if (alertIndex !== -1) {
      this.alerts.splice(alertIndex, 1);
      // Update pagination
      this.alertPagination.totalItems = Math.max(0, this.alertPagination.totalItems - 1);
      this.alertPagination.totalPages = Math.ceil(this.alertPagination.totalItems / this.alertPagination.itemsPerPage);
      this.stats.activeAlerts = this.alertPagination.totalItems;
    }
    
    // Call the backend to resolve the alert
    this.turbineService.resolveAlert(alertId).subscribe({
      next: () => {
        console.log('Alert resolved successfully');
        // Reload alerts to ensure consistency with backend
        this.loadAlerts();
        // Also reload dashboard stats if on dashboard page
        if (this.currentPage === 'dashboard') {
          this.loadData();
        }
      },
      error: (err) => {
        console.error('Failed to resolve alert', err);
        // Reload alerts to restore the original state if there was an error
        this.loadAlerts();
        // Show error message to user
        const errorMessage = err.status === 404 
          ? 'Alert not found. It may have already been resolved.' 
          : 'Failed to resolve alert. Please try again.';
        window.alert(errorMessage);
      }
    });
  }
  
  getHealthyCount(): number {
    if (this.currentPage === 'dashboard') {
      // Dashboard: count healthy turbines (ACTIVE without alerts)
      const activeTurbines = this.turbines.filter(t => t.status === 'ACTIVE');
      const turbinesWithAlerts = new Set(this.alerts.map(a => a.turbine?.id).filter((id): id is number => id !== undefined));
      return activeTurbines.filter(t => t.id !== undefined && !turbinesWithAlerts.has(t.id)).length;
    } else if (this.currentPage === 'analytics') {
      // Analytics: count all ACTIVE turbines
      return this.turbines.filter(t => t.status === 'ACTIVE').length;
    }
    return 0;
  }
  
  getWarningCount(): number {
    if (this.currentPage !== 'dashboard') return 0;
    // Count ACTIVE turbines that have MEDIUM or LOW severity alerts (but not critical)
    const activeTurbines = this.turbines.filter(t => t.status === 'ACTIVE');
    const criticalAlerts = this.alerts.filter(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');
    const turbinesWithCritical = new Set(criticalAlerts.map(a => a.turbine?.id).filter((id): id is number => id !== undefined));
    const warningAlerts = this.alerts.filter(a => (a.severity === 'MEDIUM' || a.severity === 'LOW') && 
                                                   a.turbine?.id !== undefined && 
                                                   !turbinesWithCritical.has(a.turbine.id));
    const turbinesWithWarning = new Set(warningAlerts.map(a => a.turbine?.id).filter((id): id is number => id !== undefined));
    return turbinesWithWarning.size;
  }
  
  getCriticalCount(): number {
    if (this.currentPage !== 'dashboard') return 0;
    // Count ACTIVE turbines that have CRITICAL or HIGH severity alerts
    const activeTurbines = this.turbines.filter(t => t.status === 'ACTIVE');
    const criticalAlerts = this.alerts.filter(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');
    const turbinesWithCritical = new Set(criticalAlerts.map(a => a.turbine?.id).filter((id): id is number => id !== undefined));
    return turbinesWithCritical.size;
  }
  
  getOfflineCount(): number {
    if (this.currentPage !== 'dashboard') return 0;
    return this.turbines.filter(t => t.status === 'INACTIVE' || t.status === 'OFFLINE' || t.status === 'MAINTENANCE').length;
  }
  
  getHealthyPercent(): number {
    if (this.turbines.length === 0) return 0;
    return (this.getHealthyCount() / this.turbines.length) * 100;
  }
  
  getWarningPercent(): number {
    if (this.turbines.length === 0) return 0;
    return (this.getWarningCount() / this.turbines.length) * 100;
  }
  
  getCriticalPercent(): number {
    if (this.turbines.length === 0) return 0;
    return (this.getCriticalCount() / this.turbines.length) * 100;
  }
  
  getOfflinePercent(): number {
    if (this.turbines.length === 0) return 0;
    return (this.getOfflineCount() / this.turbines.length) * 100;
  }
  

  getDonutChartPath(status: string): string {
    const total = this.turbines.length;
    const centerX = 100;
    const centerY = 100;
    const radius = 80;
    const innerRadius = 56;
    
    // If no turbines, show a full empty ring
    if (total === 0) {
      if (status === 'offline') {
        // Show a full gray ring for empty state
        const startAngleRad = (-90) * Math.PI / 180;
        const endAngleRad = (270) * Math.PI / 180;
        const x1 = centerX + radius * Math.cos(startAngleRad);
        const y1 = centerY + radius * Math.sin(startAngleRad);
        const x2 = centerX + radius * Math.cos(endAngleRad);
        const y2 = centerY + radius * Math.sin(endAngleRad);
        const x3 = centerX + innerRadius * Math.cos(endAngleRad);
        const y3 = centerY + innerRadius * Math.sin(endAngleRad);
        const x4 = centerX + innerRadius * Math.cos(startAngleRad);
        const y4 = centerY + innerRadius * Math.sin(startAngleRad);
        return `M ${x1} ${y1} A ${radius} ${radius} 0 1 1 ${x2} ${y2} L ${x3} ${y3} A ${innerRadius} ${innerRadius} 0 1 0 ${x4} ${y4} Z`;
      }
      return '';
    }
    
    const healthy = this.getHealthyCount();
    const warning = this.getWarningCount();
    const critical = this.getCriticalCount();
    const offline = this.getOfflineCount();
    
    let startAngle = 0;
    let endAngle = 0;
    let angleRange = 0;
    
    switch(status) {
      case 'healthy':
        if (healthy === 0) return '';
        startAngle = -90; // Start from top
        angleRange = (healthy / total) * 360;
        endAngle = startAngle + angleRange;
        break;
      case 'warning':
        if (warning === 0) return '';
        startAngle = -90 + (healthy / total) * 360;
        angleRange = (warning / total) * 360;
        endAngle = startAngle + angleRange;
        break;
      case 'critical':
        if (critical === 0) return '';
        startAngle = -90 + ((healthy + warning) / total) * 360;
        angleRange = (critical / total) * 360;
        endAngle = startAngle + angleRange;
        break;
      case 'offline':
        if (offline === 0) return '';
        startAngle = -90 + ((healthy + warning + critical) / total) * 360;
        angleRange = (offline / total) * 360;
        endAngle = startAngle + angleRange;
        break;
      default:
        return '';
    }
    
    if (angleRange <= 0) return '';
    
    // Handle full circle (360 degrees) - use two arcs
    if (angleRange >= 360) {
      // For full circle, draw two 180-degree arcs
      const midAngle = startAngle + 180;
      const startAngleRad = startAngle * Math.PI / 180;
      const midAngleRad = midAngle * Math.PI / 180;
      const endAngleRad = endAngle * Math.PI / 180;
      
      const x1 = centerX + radius * Math.cos(startAngleRad);
      const y1 = centerY + radius * Math.sin(startAngleRad);
      const x2 = centerX + radius * Math.cos(midAngleRad);
      const y2 = centerY + radius * Math.sin(midAngleRad);
      const x3 = centerX + radius * Math.cos(endAngleRad);
      const y3 = centerY + radius * Math.sin(endAngleRad);
      
      const x4 = centerX + innerRadius * Math.cos(endAngleRad);
      const y4 = centerY + innerRadius * Math.sin(endAngleRad);
      const x5 = centerX + innerRadius * Math.cos(midAngleRad);
      const y5 = centerY + innerRadius * Math.sin(midAngleRad);
      const x6 = centerX + innerRadius * Math.cos(startAngleRad);
      const y6 = centerY + innerRadius * Math.sin(startAngleRad);
      
      return `M ${x1} ${y1} A ${radius} ${radius} 0 1 1 ${x2} ${y2} A ${radius} ${radius} 0 1 1 ${x3} ${y3} L ${x4} ${y4} A ${innerRadius} ${innerRadius} 0 1 0 ${x5} ${y5} A ${innerRadius} ${innerRadius} 0 1 0 ${x6} ${y6} Z`;
    }
    
    const startAngleRad = startAngle * Math.PI / 180;
    const endAngleRad = endAngle * Math.PI / 180;
    
    const x1 = centerX + radius * Math.cos(startAngleRad);
    const y1 = centerY + radius * Math.sin(startAngleRad);
    const x2 = centerX + radius * Math.cos(endAngleRad);
    const y2 = centerY + radius * Math.sin(endAngleRad);
    
    const x3 = centerX + innerRadius * Math.cos(endAngleRad);
    const y3 = centerY + innerRadius * Math.sin(endAngleRad);
    const x4 = centerX + innerRadius * Math.cos(startAngleRad);
    const y4 = centerY + innerRadius * Math.sin(startAngleRad);
    
    const largeArc = angleRange > 180 ? 1 : 0;
    
    return `M ${x1} ${y1} A ${radius} ${radius} 0 ${largeArc} 1 ${x2} ${y2} L ${x3} ${y3} A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${x4} ${y4} Z`;
  }
  
  getDonutChartColor(status: string): string {
    switch(status) {
      case 'healthy': return '#10b981';
      case 'warning': return '#f59e0b';
      case 'critical': return '#ef4444';
      case 'offline': return '#6b7280';
      default: return '#9e9e9e';
    }
  }
  
  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }
  
  loadAnalytics(): void {
    if (this.currentPage !== 'analytics') {
      return;
    }
    
    this.loadTurbinesForAnalytics();
  }
  
  loadTurbinesForAnalytics(): void {
    this.turbineService.getTurbines(
      this.analyticsFilters.farm,
      this.analyticsFilters.region,
      undefined
    ).subscribe({
      next: (response) => {
        if (Array.isArray(response)) {
          this.turbines = response;
        } else if (this.isPageResponse(response)) {
          this.turbines = response.content;
        }
        this.generateAnalyticsData();
      },
      error: (err) => {
        console.error('Error loading turbines for analytics:', err);
        this.turbines = [];
        this.analyticsData = [];
      }
    });
  }
  
  generateAnalyticsData(): void {
    if (this.currentPage !== 'analytics') {
      return;
    }
    
    const days = parseInt(this.analyticsFilters.period);
    
    if (isNaN(days) || days < 1 || days > 365) {
      alert('Please enter a valid period between 1 and 365 days');
      this.analyticsFilters.period = '30';
      return;
    }
    
    const endDate = new Date();
    endDate.setHours(23, 59, 59, 999);
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - (days - 1)); // Include today
    startDate.setHours(0, 0, 0, 0);
    
    const startTime = startDate.toISOString();
    const endTime = endDate.toISOString();
    
    const farm = this.analyticsFilters.farm || undefined;
    
    // Load daily metrics from backend (all calculations done in backend)
    this.turbineService.getDailyMetricsForAnalytics(startTime, endTime, farm, undefined).subscribe({
      next: (dailyMetrics) => {
        try {
          // Backend returns pre-calculated daily metrics
          if (dailyMetrics && Array.isArray(dailyMetrics)) {
            this.analyticsData = dailyMetrics.map(metric => ({
              date: metric.date || '',
              displayDate: metric.date ? new Date(metric.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : '',
              fullDate: metric.date || '',
              farm: metric.farm || 'Unknown',
              generation: metric.totalGeneration || 0,
              efficiency: metric.avgEfficiency || 0,
              hours: metric.operatingHours || 0,
              maxPower: metric.maxPower || 0
            }));
            
            // Calculate average efficiency from "All Farms" rows only (to match graph)
            const allFarmsRows = this.analyticsData.filter(d => d.farm === 'All Farms' && d.efficiency > 0);
            if (allFarmsRows.length > 0) {
              const efficiencies = allFarmsRows.map(d => d.efficiency);
              this.avgEfficiency = efficiencies.reduce((a, b) => a + b, 0) / efficiencies.length;
            } else {
              this.avgEfficiency = 0;
            }
          } else {
            this.analyticsData = [];
            this.avgEfficiency = 0;
          }
        } catch (error) {
          console.error('Error processing daily metrics:', error);
          this.analyticsData = [];
        }
      },
      error: (err) => {
        console.error('Error loading daily metrics:', err);
        this.analyticsData = [];
      }
    });
    
            // Load graph data from backend (all calculations done in backend)
            this.turbineService.getGraphData(startTime, endTime, farm, undefined).subscribe({
              next: (graphData) => {
                try {
                  // Backend returns pre-calculated graph data
                  if (graphData && Array.isArray(graphData)) {
                    this.graphData = graphData.map(data => {
                      const dateStr = data.date || '';
                      const dateObj = dateStr ? new Date(dateStr) : new Date();
                      return {
                        date: dateStr,
                        displayDate: dateStr ? dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : '',
                        fullDate: dateStr,
                        generation: data.generation || 0,
                        efficiency: data.efficiency || 0
                      };
                    });
                    
                    // Calculate min/max values from actual graph data for proper Y-axis scaling
                    if (this.graphData.length > 0) {
                      const generations = this.graphData.map(d => d.generation).filter(g => g > 0);
                      const efficiencies = this.graphData.map(d => d.efficiency).filter(e => e > 0);
                      
                      if (generations.length > 0) {
                        this.maxGeneration = Math.max(...generations);
                        this.minGeneration = Math.min(...generations);
                        // Add padding: 10% above max, ensure min is at least 0
                        const generationRange = this.maxGeneration - this.minGeneration;
                        this.maxGeneration = this.maxGeneration + (generationRange * 0.1);
                        this.minGeneration = Math.max(0, this.minGeneration - (generationRange * 0.1));
                      } else {
                        this.maxGeneration = 1000;
                        this.minGeneration = 0;
                      }
                      
                      if (efficiencies.length > 0) {
                        this.maxEfficiency = Math.max(...efficiencies);
                        this.minEfficiency = Math.min(...efficiencies);
                        // Add padding: 10% above max, ensure min is at least 0
                        const efficiencyRange = this.maxEfficiency - this.minEfficiency;
                        this.maxEfficiency = Math.min(100, this.maxEfficiency + (efficiencyRange * 0.1));
                        this.minEfficiency = Math.max(0, this.minEfficiency - (efficiencyRange * 0.1));
                      } else {
                        this.maxEfficiency = 100;
                        this.minEfficiency = 0;
                      }
                    } else {
                      // Reset to defaults if no data
                      this.maxGeneration = 1000;
                      this.minGeneration = 0;
                      this.maxEfficiency = 100;
                      this.minEfficiency = 0;
                    }
                    
                  } else {
                    this.graphData = [];
                    this.maxGeneration = 1000;
                    this.minGeneration = 0;
                    this.maxEfficiency = 100;
                    this.minEfficiency = 0;
                  }
                } catch (error) {
                  console.error('Error processing graph data:', error);
                  this.graphData = [];
                  this.maxGeneration = 1000;
                  this.minGeneration = 0;
                  this.maxEfficiency = 100;
                  this.minEfficiency = 0;
                }
              },
              error: (err) => {
                console.error('Error loading graph data:', err);
                this.graphData = [];
                this.maxGeneration = 1000;
                this.minGeneration = 0;
                this.maxEfficiency = 100;
                this.minEfficiency = 0;
              }
            });
  }
  
  getGenerationY(generation: number): number {
    if (this.graphData.length === 0) return 340;
    if (generation === 0 || this.maxGeneration === 0) return 340;
    if (this.maxGeneration === this.minGeneration && this.maxGeneration > 0) {
      // Single data point: place at top label position (y=25)
      return 25;
    }
    const range = this.maxGeneration - this.minGeneration || 1;
    const normalized = (generation - this.minGeneration) / range;
    // Y-axis labels are at: y=25 (top, index 0), y=105, y=185, y=265, y=345 (bottom, index 4)
    // Chart area: y=25 (top) to y=340 (bottom) = 315 pixels
    // Invert: max value (normalized=1) should be at y=25, min (normalized=0) at y=340
    return 340 - (normalized * 315);
  }
  
  getEfficiencyY(efficiency: number): number {
    if (this.graphData.length === 0) return 340;
    if (efficiency === 0 || this.maxEfficiency === 0) return 340;
    if (this.maxEfficiency === this.minEfficiency && this.maxEfficiency > 0) {
      // Single data point: place at top label position (y=25)
      return 25;
    }
    const range = this.maxEfficiency - this.minEfficiency || 1;
    const normalized = (efficiency - this.minEfficiency) / range;
    // Y-axis labels are at: y=25 (top, index 0), y=105, y=185, y=265, y=345 (bottom, index 4)
    // Chart area: y=25 (top) to y=340 (bottom) = 315 pixels
    // Invert: max value (normalized=1) should be at y=25, min (normalized=0) at y=340
    return 340 - (normalized * 315);
  }
  
  getGenerationPath(): string {
    if (this.graphData.length === 0) return '';
    
    // Filter out zero values - only show points with actual data
    const points: Array<{ x: number; y: number }> = [];
    this.graphData.forEach((data, i) => {
      if (data.generation > 0) {
        const x = this.getChartX(i);
        const y = this.getGenerationY(data.generation);
        points.push({ x, y });
      }
    });
    
    if (points.length === 0) return '';
    if (points.length === 1) {
      // Single point: draw a small horizontal line
      const p = points[0];
      return `M ${p.x - 10} ${p.y} L ${p.x + 10} ${p.y}`;
    }
    
    // Multiple points: connect them with lines
    return points.map((p, idx) => {
      return `${idx === 0 ? 'M' : 'L'} ${p.x} ${p.y}`;
    }).join(' ');
  }
  
  getGenerationAreaPath(): string {
    if (this.graphData.length === 0) return '';
    const path = this.getGenerationPath();
    if (path === '') return ''; // No valid data points
    
    // Find the first and last valid data points
    const validPoints: Array<{ x: number; y: number }> = [];
    this.graphData.forEach((data, i) => {
      if (data.generation > 0) {
        validPoints.push({ x: this.getChartX(i), y: this.getGenerationY(data.generation) });
      }
    });
    
    if (validPoints.length === 0) return '';
    if (validPoints.length === 1) {
      // Single point: draw a small area
      const p = validPoints[0];
      return `${path} L ${p.x} 340 L ${p.x - 10} 340 Z`;
    }
    
    // Multiple points: close the area (bottom of chart is y=340)
    const firstX = validPoints[0].x;
    const lastX = validPoints[validPoints.length - 1].x;
    return `${path} L ${lastX} 340 L ${firstX} 340 Z`;
  }
  
  getEfficiencyPath(): string {
    if (this.graphData.length === 0) return '';
    
    // Filter out zero values - only show points with actual data
    const points: Array<{ x: number; y: number }> = [];
    this.graphData.forEach((data, i) => {
      if (data.efficiency > 0) {
        const x = this.getChartX(i);
        const y = this.getEfficiencyY(data.efficiency);
        points.push({ x, y });
      }
    });
    
    if (points.length === 0) return '';
    if (points.length === 1) {
      // Single point: draw a small horizontal line
      const p = points[0];
      return `M ${p.x - 10} ${p.y} L ${p.x + 10} ${p.y}`;
    }
    
    // Multiple points: connect them with lines
    return points.map((p, idx) => {
      return `${idx === 0 ? 'M' : 'L'} ${p.x} ${p.y}`;
    }).join(' ');
  }
  
  getEfficiencyAreaPath(): string {
    if (this.graphData.length === 0) return '';
    const path = this.getEfficiencyPath();
    if (path === '') return ''; // No valid data points
    
    // Find the first and last valid data points
    const validPoints: Array<{ x: number; y: number }> = [];
    this.graphData.forEach((data, i) => {
      if (data.efficiency > 0) {
        validPoints.push({ x: this.getChartX(i), y: this.getEfficiencyY(data.efficiency) });
      }
    });
    
    if (validPoints.length === 0) return '';
    if (validPoints.length === 1) {
      // Single point: draw a small area
      const p = validPoints[0];
      return `${path} L ${p.x} 340 L ${p.x - 10} 340 Z`;
    }
    
    // Multiple points: close the area (bottom of chart is y=340)
    const firstX = validPoints[0].x;
    const lastX = validPoints[validPoints.length - 1].x;
    return `${path} L ${lastX} 340 L ${firstX} 340 Z`;
  }
  
  getChartX(index: number): number {
    if (this.graphData.length === 0) return 50;
    if (this.graphData.length === 1) return 50;
    return (index / (this.graphData.length - 1)) * 900 + 50;
  }
  
  getGenerationYLabel(index: number): string {
    if (this.graphData.length === 0 || this.maxGeneration === 0) return '0';
    const range = this.maxGeneration - this.minGeneration || 1;
    const value = this.maxGeneration - (range * index / 4);
    if (value < 0) return '0'; // Prevent negative values
    if (value >= 1000) {
      return (value / 1000).toFixed(1) + 'k';
    }
    return Math.round(value).toString();
  }
  
  getEfficiencyYLabel(index: number): string {
    if (this.graphData.length === 0 || this.maxEfficiency === 0) return '0';
    const range = this.maxEfficiency - this.minEfficiency || 1;
    const value = this.maxEfficiency - (range * index / 4);
    if (value < 0) return '0'; // Prevent negative values
    return Math.round(value).toString();
  }
  
  getAvgEfficiency(): number {
    // Use the calculated avgEfficiency from "All Farms" rows (matches graph)
    if (this.avgEfficiency > 0) {
      return this.avgEfficiency;
    }
    
    // If not calculated yet, calculate from "All Farms" rows in analytics data
    if (this.analyticsData.length > 0) {
      const allFarmsRows = this.analyticsData.filter(d => d.farm === 'All Farms' && d.efficiency > 0);
      if (allFarmsRows.length > 0) {
        const efficiencies = allFarmsRows.map(d => d.efficiency);
        return efficiencies.reduce((a, b) => a + b, 0) / efficiencies.length;
      }
    }
    
    // Fallback: return 0 if no data available
    return 0;
  }
  
  loadAvgEfficiency(): void {
    // Load recent aggregates (last 24 hours) to calculate average efficiency
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 1);
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(23, 59, 59, 999);
    
    const startTime = startDate.toISOString();
    const endTime = endDate.toISOString();
    
    this.turbineService.getAggregatesByFilters(startTime, endTime, undefined, undefined).subscribe({
      next: (aggregates) => {
        if (aggregates.length === 0) {
          this.avgEfficiency = 0;
          return;
        }
        
        // Calculate average efficiency from all aggregates
        const efficiencies = aggregates
          .filter(agg => agg.avgEfficiency && agg.avgEfficiency > 0)
          .map(agg => agg.avgEfficiency);
        
        if (efficiencies.length > 0) {
          const sum = efficiencies.reduce((a, b) => a + b, 0);
          this.avgEfficiency = sum / efficiencies.length;
        } else {
          this.avgEfficiency = 0;
        }
      },
      error: (err) => {
        console.error('Error loading average efficiency:', err);
        this.avgEfficiency = 0;
      }
    });
  }
  
  getTotalCapacity(): number {
    return this.turbines.reduce((sum, t) => sum + t.ratedPower, 0);
  }
  
  ngOnInit(): void {
    try {
      const savedTheme = localStorage.getItem('theme');
      this.loadAvgEfficiency(); // Load average efficiency on component init
      // Always use light theme
      this.isDarkTheme = false;
      this.applyTheme();
      this.loadData();
      setTimeout(() => {
        if (this.currentPage === 'analytics') {
          this.loadAnalytics();
        }
      }, 1000);
    } catch (error) {
      console.error('Error in ngOnInit:', error);
    }
  }

  ngAfterViewInit(): void {
    // Charts will be initialized when data is loaded
  }

  ngOnDestroy(): void {
  }
  

  navigateToPage(page: string): void {
    this.currentPage = page;
    const tabLabels = ['dashboard', 'turbines', 'analytics', 'alerts'];
    const index = tabLabels.indexOf(page);
    if (index >= 0) {
      this.selectedTabIndex = index;
    }
    this.resetPagination();
    this.selectedTurbine = null;
    this.showDetails = false;
    
    switch(page) {
      case 'dashboard':
        this.loadData();
        break;
      case 'turbines':
        this.loadTurbines();
        break;
      case 'alerts':
        this.loadAlerts();
        break;
      case 'analytics':
        this.loadTurbinesForAnalytics();
        break;
    }
  }
  
  resetPagination(): void {
    this.pagination = {
      currentPage: 0,
      itemsPerPage: 10,
      totalItems: 0,
      totalPages: 0
    };
    
    this.alertPagination = {
      currentPage: 0,
      itemsPerPage: 10,
      totalItems: 0,
      totalPages: 0
    };
  }
  
  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkTheme ? 'dark' : 'light');
  }
  
  applyTheme(): void {
    // Always use light theme
    document.body.classList.remove('dark-theme');
    document.body.classList.add('light-theme');
  }
  
  isInvalidPeriod(): boolean {
    if (!this.analyticsFilters.period) return false;
    const days = parseInt(this.analyticsFilters.period);
    return isNaN(days) || days < 1 || days > 365;
  }
  
  Math = Math;
}

