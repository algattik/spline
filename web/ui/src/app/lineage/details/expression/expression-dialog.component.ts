/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {MAT_DIALOG_DATA} from "@angular/material";
import {Component, Inject} from "@angular/core";
import * as _ from "lodash";
import {typeOfExpr} from "../../types";
import {IExpression} from "../../../../generated-ts/expression-model";
import {ITreeNode} from 'angular-tree-component/dist/defs/api';
import {IActionMapping, ITreeOptions} from 'angular-tree-component';

@Component({
    selector: "expression-dialog",
    styleUrls: ["expression-dialog.component.less"],
    template: `
        <code>{{ exprString }}</code>
        <hr>
        <tree-root #tree [nodes]="exprTree" [options]="treeOptions">
            <ng-template #treeNodeTemplate let-node>
                <div *ngIf="!node.isExpanded">{{ node.data.text }}</div>
                <div *ngIf="node.isExpanded">{{ node.data.name }}</div>
            </ng-template>
        </tree-root>
    `
})
export class ExpressionDialogComponent {

    expr: IExpression
    exprString: string
    exprTree: any[]

    readonly actionMapping: IActionMapping = {
        mouse: {
            click: (tree, node) => ExpressionDialogComponent.onNodeClicked(node)
        }
    }

    readonly treeOptions: ITreeOptions = {
        actionMapping: this.actionMapping,
        allowDrag: false,
        allowDrop: false,
    }

    constructor(@Inject(MAT_DIALOG_DATA) data: any) {
        this.expr = data.expr
        this.exprString = data.exprString
        this.exprTree = this.buildExprTree()
    }

    private buildExprTree(): any[] {
        let seq = 0

        function buildChildren(ex: IExpression): (any[] | undefined) {
            let et = typeOfExpr(ex)
            // todo: improve expression view for specific expression types
            return buildChildrenForGenericExpression(ex.children || [])
        }

        function buildChildrenForGenericExpression(subExprs: IExpression[]): any[] {
            return subExprs.map(buildNode)
        }

        function buildNode(expr: IExpression) {
            return {
                id: seq++,
                name: _.isEmpty(expr.children)
                    ? expr.text // only use it for leaf expressions
                    : expr.exprType, // todo: this property is not mandatory for any arbitrary expression
                text: expr.text.replace(/#\d+/g, ""),
                children: buildChildren(expr)
            }
        }

        return [buildNode(this.expr)]
    }

    static onNodeClicked(node: ITreeNode) {
        node.toggleExpanded()
    }

}