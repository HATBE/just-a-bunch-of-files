import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-manage-profile-tab',
  templateUrl: './manage-profile-tab.component.html',
  styleUrls: ['./manage-profile-tab.component.css']
})
export class ManageProfileTabComponent implements OnInit {
  isLoading: boolean = false;
  error: string = "";

  profileSettingsForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UsersService,
  ) {
    this.profileSettingsForm = this.formBuilder.group({
      a: '',
      b: '',
      c: ''
    });
  }

  ngOnInit(): void {
  }

  onSubmitProfileSettings() {

  }

  clearForm() {
    this.profileSettingsForm.reset()
  }

}
