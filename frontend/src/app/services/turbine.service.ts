import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Turbine, Farm, Telemetry, TelemetryAggregate, HealthAlert, PageResponse } from '../models/turbine.model';

@Injectable({
  providedIn: 'root'
})
export class TurbineService {
  private apiUrl = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  getTurbines(farm?: string, region?: string, status?: string, page?: number, size?: number): Observable<Turbine[] | PageResponse<Turbine>> {
    let params = new HttpParams();
    if (farm) params = params.set('farm', farm);
    if (region) params = params.set('region', region);
    if (status) params = params.set('status', status);
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    return this.http.get<Turbine[] | PageResponse<Turbine>>(`${this.apiUrl}/turbines`, { params });
  }
  
  getTurbineById(id: number): Observable<Turbine> {
    return this.http.get<Turbine>(`${this.apiUrl}/turbines/${id}`);
  }
  
  getFarms(): Observable<Farm[]> {
    return this.http.get<Farm[]>(`${this.apiUrl}/farms`);
  }
  
  createFarm(farm: Farm): Observable<Farm> {
    return this.http.post<Farm>(`${this.apiUrl}/farms`, farm);
  }
  
  getFarmsByRegion(region: string): Observable<Farm[]> {
    return this.http.get<Farm[]>(`${this.apiUrl}/farms/region/${region}`);
  }
  
  getHealthStatus(turbineId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/health/turbine/${turbineId}/status`);
  }
  
  getActiveAlerts(turbineId?: number, region?: string, farm?: string, page?: number, size?: number): Observable<HealthAlert[] | PageResponse<HealthAlert>> {
    let params = new HttpParams();
    if (turbineId) params = params.set('turbineId', turbineId.toString());
    if (region) params = params.set('region', region);
    if (farm) params = params.set('farm', farm);
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    return this.http.get<HealthAlert[] | PageResponse<HealthAlert>>(`${this.apiUrl}/health/alerts`, { params });
  }
  
  getDailyMetrics(turbineId: number, date: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/turbine/${turbineId}/daily`, {
      params: { date }
    });
  }
  
  getHistoricalPerformance(turbineId: number, startDate: string, endDate: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/turbine/${turbineId}/historical`, {
      params: { startDate, endDate }
    });
  }
  
  getAggregates(turbineId: number, startTime: string, endTime: string): Observable<TelemetryAggregate[]> {
    return this.http.get<TelemetryAggregate[]>(`${this.apiUrl}/analytics/turbine/${turbineId}/aggregates`, {
      params: { startTime, endTime }
    });
  }
  
  getAggregatesByFilters(startTime: string, endTime: string, farm?: string, region?: string): Observable<TelemetryAggregate[]> {
    let params: any = { startTime, endTime };
    if (farm) params.farm = farm;
    if (region) params.region = region;
    return this.http.get<TelemetryAggregate[]>(`${this.apiUrl}/analytics/aggregates`, { params });
  }
  
  getDailyMetricsForAnalytics(startTime: string, endTime: string, farm?: string, region?: string): Observable<any[]> {
    let params: any = { startTime, endTime };
    if (farm) params.farm = farm;
    if (region) params.region = region;
    return this.http.get<any[]>(`${this.apiUrl}/analytics/daily`, { params });
  }
  
  getGraphData(startTime: string, endTime: string, farm?: string, region?: string): Observable<any[]> {
    let params: any = { startTime, endTime };
    if (farm) params.farm = farm;
    if (region) params.region = region;
    return this.http.get<any[]>(`${this.apiUrl}/analytics/graph`, { params });
  }
  
  resolveAlert(alertId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/health/alerts/${alertId}/resolve`, {});
  }
}

