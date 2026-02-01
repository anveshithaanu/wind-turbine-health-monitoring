export interface Turbine {
  id?: number;
  turbineId: string;
  name: string;
  farm?: Farm;
  ratedPower: number;
  status: string;
  installedDate?: string;
  lastUpdated?: string;
}

export interface Farm {
  id?: number;
  name: string;
  region: string;
  location: string;
}

export interface Telemetry {
  id?: number;
  turbine?: Turbine;
  timestamp: string;
  windSpeed: number;
  powerOutput: number;
  rotorSpeed: number;
  temperature: number;
  vibration: number;
  efficiency: number;
}

export interface TelemetryAggregate {
  id?: number;
  turbine?: Turbine;
  hourStart: string;
  avgWindSpeed: number;
  avgPowerOutput: number;
  avgRotorSpeed: number;
  avgTemperature: number;
  avgVibration: number;
  avgEfficiency: number;
  totalGeneration: number;
  dataPointCount: number;
  hasAnomaly: boolean;
}

export interface HealthAlert {
  id?: number;
  turbine?: Turbine;
  alertTime: string;
  alertType: string;
  severity: string;
  message: string;
  status: string;
  resolvedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

