import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import {TodoSummary} from "./todo-summary";


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
    public owners: string[] = [];
    public categories: string[] = [];

    constructor(private http:Http) {
    }

    ngOnInit(): void {
        this.http.request(API_URL + "todoSummary").map(res => res.json()).subscribe(
            todoSum => {
                this.todoSummary = todoSum;
                for (let key in this.todoSummary.ownersPercentComplete){
                    this.owners.push(key);
                }
                for (let key in this.todoSummary.categoriesPercentComplete){
                    this.categories.push(key);
                }
            },
            err => {

                console.log(err);
            }
        )
    }

}

