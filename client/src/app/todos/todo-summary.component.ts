import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import {TodoSummary} from "./todo-summary";
import {Observable} from "rxjs";


@Component({
    selector: 'todo-summary-component',
    templateUrl: 'todo-summary.component.html',
})

export class TodoSummaryComponent implements OnInit {
    public todoSummary: TodoSummary = {
        percentToDosComplete: 0,
        categoriesPercentComplete: new Map<string,number>(),
        ownersPercentComplete: new Map<string,number>()
    };
    constructor(private http:Http) {
    }

    ngOnInit(): void {
        this.http.request(API_URL + "todoSummary").map(res => res.json()).subscribe(
            todoSum => { this.todoSummary = todoSum;
                        console.log("You made it here");},
            err => {

                console.log(err);
            }
        )
    }

}

