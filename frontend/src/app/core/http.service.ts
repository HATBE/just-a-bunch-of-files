import {inject} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { firstValueFrom, Observable, throwError } from 'rxjs';

export type ApiError = {
  status: number;
  code: string;
  message: string;
};

export abstract class HttpService {
  protected readonly http = inject(HttpClient);
  private readonly BASE_URL = 'http://localhost:8080/api/v1';

  protected entity!: string;

  protected setEntity(entity: string) {
    this.entity = entity;
  }

  protected createUrl(path: string[] = []): string {
    const cleanPath = path.filter(p => p && p.trim().length > 0);
    return [this.BASE_URL, this.entity, ...cleanPath].join('/');
  }

  protected getRaw<T>(path: string[] = [], /*, filter?: BaseFilter*/): Observable<T> {
    return this.http
      .get<T>(this.createUrl(path)/*, { params: this.createParams(filter as any) }*/)
      .pipe(catchError(this.mapError));
  }

  protected postRaw<TResponse, TBody>(path: string[] = [], body: TBody): Observable<TResponse> {
    return this.http
      .post<TResponse>(this.createUrl(path), body)
      .pipe(catchError(this.mapError));
  }

  protected putRaw<TResponse, TBody>(path: string[] = [], body: TBody): Observable<TResponse> {
    return this.http
      .put<TResponse>(this.createUrl(path), body)
      .pipe(catchError(this.mapError));
  }

  protected patchRaw<TResponse, TBody>(path: string[] = [], body: TBody): Observable<TResponse> {
    return this.http
      .patch<TResponse>(this.createUrl(path), body)
      .pipe(catchError(this.mapError));
  }

  protected deleteRaw<T>(path: string[] = []): Observable<T> {
    return this.http
      .delete<T>(this.createUrl(path))
      .pipe(catchError(this.mapError));
  }

  protected get<T>(path: string[] = []/*, filter?: BaseFilter*/): Promise<T> {
    return firstValueFrom(this.getRaw<T>(path/*, filter*/));
  }

  protected post<TResponse, TBody>(path: string[] = [], body: TBody): Promise<TResponse> {
    return firstValueFrom(this.postRaw<TResponse, TBody>(path, body));
  }

  protected put<TResponse, TBody>(path: string[] = [], body: TBody): Promise<TResponse> {
    return firstValueFrom(this.putRaw<TResponse, TBody>(path, body));
  }

  protected patch<TResponse, TBody>(path: string[] = [], body: TBody): Promise<TResponse> {
    return firstValueFrom(this.patchRaw<TResponse, TBody>(path, body));
  }

  protected delete<T>(path: string[] = []): Promise<T> {
    return firstValueFrom(this.deleteRaw<T>(path));
  }

  private mapError = (err: HttpErrorResponse) => {
    const api = err.error as any;

    const normalized: ApiError = {
      status: err.status,
      code: api?.code ?? api?.errorCode ?? 'HTTP_ERROR',
      message: api?.message ?? api?.error ?? err.message ?? 'Request failed',
    };

    return throwError(() => normalized);
  };
}
