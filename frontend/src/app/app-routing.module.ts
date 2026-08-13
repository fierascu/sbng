import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RegisterationComponent} from './registeration/registeration.component';
import {TodosComponent} from './todos/todos.component';

const routes: Routes = [
  {
    path: '',
    component: TodosComponent,
    pathMatch: "full"
  },
  {
    path: 'reg',
    component: RegisterationComponent,
    pathMatch: "full"
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
