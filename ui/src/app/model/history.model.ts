import {User} from "./user.model";

export interface History{
  id: number;
  userId: string;
  date: Date;
  type: HistoryType;
}

export interface AdminLoginLogout {
  userId: string;
  date: number;
  login: string;
  logout: string;
}

export enum HistoryType{
  Login = 'Login',
  Logout = 'Logout'
}

export type UserHistory = [User, History[]]

