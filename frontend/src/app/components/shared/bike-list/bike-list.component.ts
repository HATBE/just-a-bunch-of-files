import { Component, OnInit, Input } from '@angular/core';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-bike-list',
  templateUrl: './bike-list.component.html',
  styleUrls: ['./bike-list.component.css']
})
export class BikeListComponent implements OnInit {
  @Input() userId: number | null = null;

  isLoading: boolean = false;
  error: string = '';
  data: any = null;

  constructor(
    private usersService: UsersService,
  ) { }

  ngOnInit(): void {
    this.isLoading = true;

    this.usersService.getBikesFromUser(this.userId).subscribe({
      next: (data: any) => {
        this.isLoading = false;
        this.data = data.data

        console.log(this.data)
      },
      error: (data: any) => {
        this.error = data.error.message || data.statusText || 'Unknown error';
        this.isLoading = false;
      }
    });
  }

}
