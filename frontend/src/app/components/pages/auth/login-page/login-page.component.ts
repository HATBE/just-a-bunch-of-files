import { Component, OnInit, Renderer2 } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { faArrowRight } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { Emitters } from '../../../../emitters/emitters';

@Component({
  selector: 'app-login',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {
  faArrowRight = faArrowRight;

  form: FormGroup;
  error: string = '';
  isLoggedIn: boolean = false;
  isLoading: boolean = false;
  freshLoggedInName: string | null = null;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router,
    private renderer: Renderer2
  ) {
    this.form = this.formBuilder.group({
      username: '',
      password: ''
    });
  }

  ngOnInit(): void {
    // check if user is already loggedin
    if(this.authService.isLoggedIn()) {
      this.isLoggedIn = true;
      this.router.navigate(['/']);
      return;
    };

    if(window.location.search) {
      const param = new URLSearchParams(window.location.search);

      if(param.has('freshregisteras')) {
        this.form.patchValue({username: param.get("freshregisteras")});
        this.freshLoggedInName = param.get("freshregisteras");

        this.renderer.selectRootElement('#passwordInput').focus(); // select password field if user freshly registred
      }
    }
  }

  onSubmitLogin() {
    this.isLoading = true;
    const username = this.form.getRawValue().username;
    const password = this.form.getRawValue().password;

    if(username === '' || password === '') {
      this.isLoading = false;
      return;
    }

    this.authService.login(username, password)
    .subscribe({
      next: (data: any) => {
        localStorage.setItem('authtoken', data.data.token); // save token in localstorage
        localStorage.setItem('username', data.data.user.username);
        localStorage.setItem('user_id', data.data.user.id);

        setTimeout(() => {
          Emitters.authEmitter.emit(true);
          this.isLoading = false;
          window.location.href = "/";
        }, 500); // 0.5 sec timeout (until browser saved token)
      },
      error: (data: {error: {message: string}, statusText: string}) => {
        this.isLoading = false;
        this.error = data.error.message || data.statusText || 'Unknown error';
      }
    });
  }
}
