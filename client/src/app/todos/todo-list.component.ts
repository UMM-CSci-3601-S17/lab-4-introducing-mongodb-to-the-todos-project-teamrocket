import { Component, OnInit } from '@angular/core';
import { TodoListService } from "./todo-list.service";
import { Todo } from "./todo";
import { FilterBy } from "./filter.pipe";
import {Observable} from "rxjs";
import {forEach} from "@angular/router/src/utils/collection";

@Component({
    selector: 'todo-list-component',
    templateUrl: 'todo-list.component.html',
    providers: [ FilterBy ]
})

export class TodoListComponent implements OnInit {
    public todos: Todo[];
    public worked = false;

    constructor(private todoListService: TodoListService) {
        //this.todos = this.todoListService.getTodos();
    }

    ngOnInit(): void {
        this.todoListService.getTodos().subscribe(
            todos => this.todos = todos,
            err => {
                console.log(err);
            }
        );
    }

    public clicked(owner: string, status: string, category: string, body: string): void {
        let i = 0;
        let todoArray: Todo[] = [];

        this.todoListService.filterTodos("Blanche", "sunt", "incomplete", "software design").subscribe(
            todos => todoArray[i++] = todos,
            err => {
                console.log(err);
            }
        );
        i = 0;

        for (let todo of todoArray){
            this.todos[i++] = todo;
        }


        console.log(todoArray);
        console.log(this.todos);
    }
}
