import { Component, OnInit } from '@angular/core';
import { UsersService } from 'src/app/services/users.service';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile-page.component.html',
  styleUrls: ['./user-profile-page.component.css'],
  providers: [
    Title
  ]
})
export class UserProfilePageComponent implements OnInit {
  isLoading: boolean = false;

  id: string | null = '';

  error: string = '';
  data: any = null;

  constructor(
    private usersService: UsersService,
    private route: ActivatedRoute,
    private title: Title,
  ) { }

  ngOnInit(): void {
    this.isLoading = true;

    this.route.paramMap.subscribe((paramMap: ParamMap) => {
      if(paramMap.has('id')) {
        this.id = paramMap.get('id');

        this.usersService.getUser(this.id).subscribe({
          next: (data: any) => {
            this.isLoading = false;
            this.data = data.data
            this.title.setTitle(`Profile of ${this.data.user.username}`);
          },
          error: (data: any) => {
            this.error = data.error.message || data.statusText || 'Unknown error';
            this.isLoading = false;
            this.title.setTitle(`Error loading user!`);
          }
        });
      }
    });
  }

}
