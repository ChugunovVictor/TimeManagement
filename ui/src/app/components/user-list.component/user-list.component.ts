import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {User, UserType} from "../../model/user.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {

  userType = UserType
  userList: User[] = []
  targetUser: User | null = null;

  // @ts-ignore
  @ViewChild('modal') modal: ElementRef;
  // @ts-ignore
  @ViewChild('password') password: ElementRef;

  userForm = new FormGroup({
    id: new FormControl(null),
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    type: new FormControl('', Validators.required),
    email: new FormControl(''),
    password: new FormControl(''),
    isActive: new FormControl(true),
  });

  cancel() {
    this.userForm.reset();
    this.modal.nativeElement.classList.add('hidden')
    this.userForm.controls['email'].setValidators(null);
    this.userForm.controls['password'].setValidators(null);

  }

  add() {
    this.modal.nativeElement.classList.remove('hidden')
  }

  edit(user: User) {
    this.targetUser = user;
    this.userForm.patchValue(user)
    this.modal.nativeElement.classList.remove('hidden')
  }

  delete(user: User) {
    this.userService.delete(user).subscribe(result => {
      this.loadData();
    });
  }

  save() {
    this.userForm.markAllAsTouched()
    if (this.userForm.invalid) {
      console.log(this.findInvalidControls());
      return;
    }

    let result = {...this.userForm.value}
    if (!result.id) result.id = (new Date()).getTime().toString()
    if (!result.isActive) result.isActive = false; else result.isActive = true;
    if (result.password) result.password = Number(result.password);
    this.userService.save(result).subscribe(result => {
      this.userForm.reset();
      this.userForm.controls['email'].setValidators(null);
      this.userForm.controls['password'].setValidators(null);
      this.modal.nativeElement.classList.add('hidden');
      this.targetUser = null;
      this.loadData();
    });
  }

  public findInvalidControls() {
    const invalid = [];
    const controls = this.userForm.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    return invalid;
  }

  passwordCheck(event: KeyboardEvent) {
    if (event.keyCode >= 48 && event.keyCode <= 57) {
    } else event.preventDefault();
  }

  loadData(): void {
    this.userService.load().subscribe(result => this.userList = result);
  }

  constructor(private userService: UserService) {
  }

  ngOnInit() {
    this.loadData();

    this.userForm.controls['type'].valueChanges.subscribe(
      result => {
        this.userForm.controls['email'].reset()
        this.userForm.controls['password'].reset()

        if (result === 'Manager') {
          this.userForm.controls['email'].setValidators([Validators.required, Validators.email]);
          this.userForm.controls['password'].setValidators([Validators.maxLength(4)]);
        } else {
          this.userForm.controls['password'].setValidators([Validators.required, Validators.maxLength(4)]);
          this.userForm.controls['email'].setValidators([Validators.email]);
        }
      }
    )
  }

}
