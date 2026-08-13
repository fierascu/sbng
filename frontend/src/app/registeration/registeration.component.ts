import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-registeration',
  templateUrl: './registeration.component.html',
  styleUrls: ['./registeration.component.scss']
})
export class RegisterationComponent implements OnInit {
  registerationForm: FormGroup;

  constructor () {
    this.registerationForm = this.initForm()
  }

  ngOnInit(): void {
    this.initForm();
  }

  initForm() {
    return new FormGroup({
      firstName: new FormControl<string>('', [Validators.required]),
      lastName: new FormControl<string>('', [Validators.required]),
      userName: new FormControl<string>('', [Validators.required]),
      password: new FormControl<string>('', [Validators.required]),
    })
  }

  onSubmit() {
    console.log(this.registerationForm.value);
  }

}
