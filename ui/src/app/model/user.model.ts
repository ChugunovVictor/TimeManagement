export interface User{
  id: string;
  firstName: string;
  lastName: string;
  type: UserType;
  email?: string;
  password?: number;
  isActive: boolean;
}

export enum UserType{
  Manager = 'Manager',
  Machanic = 'Mechanic'
}
