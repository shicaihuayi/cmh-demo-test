/// <reference types="vite/client" />

declare module 'axios' {
    export interface AxiosRequestConfig {
        suppressErrorHandler?: boolean;
    }
}
