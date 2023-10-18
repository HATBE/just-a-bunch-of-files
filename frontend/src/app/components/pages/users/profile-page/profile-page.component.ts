import { Component, OnInit } from '@angular/core';
import { UsersService } from 'src/app/services/users.service';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css'],
  providers: [
    Title
  ]
})
export class ProfilePageComponent implements OnInit {
  isLoading: boolean = false;

  id: string | null = '';

  error: string = '';
  data: any = null;

  bikes = [
    {
      img: '/assets/xj6.jpg',
      name: 'Black Beast',
      make: 'Yamaha',
      model: 'XJ6NA',
      year: '2009',
      fromYear: 2023,
      toYear: null,
    },
    {
      img: '/assets/gsr750.jpg',
      name: 'Blue Beast',
      make: 'Suzuki',
      model: 'GSR 750',
      year: '2013',
      fromYear: 2022,
      toYear: null
    },
    {
      img: '/assets/ninja300.jpg',
      name: 'Green Beast',
      make: 'Kawasaki',
      model: 'Ninja 300',
      year: '2013',
      fromYear: 2022,
      toYear: 2023
    },
    {
      img: '/assets/mt125.jpg',
      name: 'The Slow Gray',
      make: 'Yamaha',
      model: 'MT-125',
      year: '2021',
      fromYear: 2021,
      toYear: 2021
    }
  ];

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
            this.title.setTitle(`Profile of ${this.data.user.username.toUpperCase()}`);
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
