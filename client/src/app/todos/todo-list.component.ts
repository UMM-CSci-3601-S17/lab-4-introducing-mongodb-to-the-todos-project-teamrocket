import { Component, OnInit } from '@angular/core';
import { TodoListService } from "./todo-list.service";
import { Todo } from "./todo";

@Component({
    selector: 'todo-list-component',
    templateUrl: 'todo-list.component.html'
})

export class TodoListComponent {
    public todos: Todo[] = [];
    public owner = "";
    public bodyContains = "";
    public category = "";
    public limit = "";
    public todoStatus = "";
    public order = "";

    constructor(private todoListService: TodoListService) {
    }


    public clicked(owner: string, status: string, category: string, body: string, orderBy: string, limit: string): void {


        this.todoListService.filterTodos(owner, body, status, category, orderBy, limit).subscribe(
            todos => { this.todos = todos;
            },
            err => {
                console.log(err);
            }
        );

    }
}
