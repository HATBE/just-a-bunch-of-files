import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css']
})
export class ProfilePageComponent implements OnInit {

  bikes = [
    {
      img: '/assets/xj6.jpg',
      name: 'Black Beast',
      make: 'Yamaha',
      model: 'XJ6NA',
      year: '2009',
      fromyear: 2023,
      toyear: null,
    },
    {
      img: '/assets/gsr750.jpg',
      name: 'Blue Beast',
      make: 'Suzuki',
      model: 'GSR 750',
      year: '2013',
      fromyear: 2022,
      toyear: null
    },
    {
      img: '/assets/ninja300.jpg',
      name: 'Green Beast',
      make: 'Kawasaki',
      model: 'Ninja 300',
      year: '2013',
      fromyear: 2022,
      toyear: 2023
    },
    {
      img: '/assets/mt125.jpg',
      name: 'The Slow Gray',
      make: 'Yamaha',
      model: 'MT-125',
      year: '2021',
      fromyear: 2021,
      toyear: 2021
    }
  ]

  constructor() { }

  ngOnInit(): void {
  }

}
