import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-register-page',
  templateUrl: './register-page.component.html',
  styleUrls: ['./register-page.component.css']
})
export class RegisterPageComponent implements OnInit {
  form: FormGroup;
  error: string = '';
  isLoggedIn: boolean = false;
  isLoading: boolean = false;


  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router,
  ) {
    this.form = this.formBuilder.group({
      username: '',
      email: '',
      password: '',
      repeatPassword: '',
      checkedTerms: true // TODO: change if agb returns!
    });
  }

  ngOnInit(): void {
    // check if user is already loggedin
    if(this.authService.isLoggedIn()) {
      this.isLoggedIn = true;
      this.router.navigate(['/']);
      return;
    };
  }

  onSubmitRegister() {
    this.isLoading = true;
    const username = this.form.getRawValue().username;
    const email_address = this.form.getRawValue().email;
    const password = this.form.getRawValue().password;
    const repeat_pasword = this.form.getRawValue().repeatPassword;
    const checkedTerms = this.form.getRawValue().checkedTerms;

    if(!checkedTerms) {
      this.isLoading = false;
      this.error = 'Bitte Akzeptiere die AGBs!';
      return;
    }

    if(password !== repeat_pasword) {
      this.isLoading = false;
      this.error = 'Die Passwörter stimmen nicht überein!';
      return;
    }

    if(username === '' || email_address === '' || password === '' || repeat_pasword === '') {
      this.isLoading = false;
      return;
    }

    this.authService.register(username, email_address, password)
    .subscribe({
      next: (data: any) => {
        // register was successfull
        setTimeout(() => {
          this.isLoading = false;
          window.location.href = `/auth/login?freshregisteras=${data.data.user.username}`;
        }, 500);
      },
      error: (data: {error: {message: string}, statusText: string}) => {
        this.isLoading = false;
        this.form.get('password')?.reset();
        this.form.get('repeatPassword')?.reset();

        this.error = data.error.message || data.statusText || 'Unknown error';
      }
    });
  }
}
