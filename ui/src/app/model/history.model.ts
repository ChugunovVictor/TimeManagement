import {User} from "./user.model";

export interface History{
  id: number;
  userId: string;
  date: Date;
  type: HistoryType;
}

export enum HistoryType{
  Login = 'Login',
  Logout = 'Logout'
}

export type UserHistory = [User, History[]]

