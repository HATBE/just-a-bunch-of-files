export interface FullBikeModel {
  id: number;
  name: string;
  user_id: number;
  make: string;
  model: string;
  year: number | null;
  toYear: number | null;
  fromYear: number;
}
