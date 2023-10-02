import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css']
})
export class ProfilePageComponent implements OnInit {

  bikes = [
    {
      name: 'Blue Beast',
      make: 'Suzuki',
      model: 'GSR 750',
      year: '2013'
    },
    {
      name: 'Black Beast',
      make: 'Yamaha',
      model: 'XJ6NA',
      year: '2009'
    },
    {
      name: 'Green Beast',
      make: 'Kawasaki',
      model: 'Ninja 300',
      year: '2013'
    },
    {
      name: 'The Slow Gray',
      make: 'Yamaha',
      model: 'MT-125',
      year: '2021'
    }
  ]

  constructor() { }

  ngOnInit(): void {
  }

}
