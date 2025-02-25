/*
 * eiam-console - Employee Identity and Access Management Program
 * Copyright © 2020-2022 TopIAM (support@topiam.cn)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
"use strict";(self.webpackChunktopiam_console=self.webpackChunktopiam_console||[]).push([[8648],{34270:function(E,o,t){t.r(o),t.d(o,{default:function(){return M}});var s=t(97983),a=t.n(s),f=t(11281),e=t.n(f),m=t(40794),r=t.n(m),i=t(25191),n=t(84865),d=t(98971),P=t(78234),C=t(69400),u=t(67038),c=t(85893),x=function(){var p=(0,d.useModel)("@@initialState"),h=p.initialState,l=p.setInitialState,F=(0,d.useLocation)();return(0,P.Z)(r()(a()().mark(function T(){return a()().wrap(function(y){for(;;)switch(y.prev=y.next){case 0:C.Z.warning({title:"\u4F1A\u8BDD\u8FC7\u671F",content:"\u60A8\u7684\u767B\u5F55\u4FE1\u606F\u5DF2\u8FC7\u671F\uFF0C\u8BF7\u91CD\u65B0\u767B\u5F55\u3002",okText:"\u786E\u8BA4",okType:"danger",centered:!1,maskClosable:!1,okCancel:!1,onOk:function(){var U=r()(a()().mark(function j(){var g,R,v,I,D,S;return a()().wrap(function(O){for(;;)switch(O.prev=O.next){case 0:return O.next=2,l(e()(e()({},h),{},{currentUser:void 0}));case 2:g=(0,u.parse)(F.search),R=g,v=R.redirect_uri,I={pathname:i.wm},D=v&&v.split("/"),v&&v!==D[0]+"//"+D[2]+"/"&&(I=e()(e()({},I),{},{search:(0,u.stringify)({redirect_uri:v})})),S=n.m.createHref(I),window.location.replace(S);case 9:case"end":return O.stop()}},j)}));function B(){return U.apply(this,arguments)}return B}()});case 1:case"end":return y.stop()}},T)}))),(0,c.jsx)(c.Fragment,{})},M=function(){return(0,c.jsx)(x,{})}},78234:function(E,o,t){var s=t(67294),a=t(92770),f=t(31663),e=function(r){f.Z&&((0,a.mf)(r)||console.error('useMount: parameter `fn` expected to be a function, but got "'.concat(typeof r,'".'))),(0,s.useEffect)(function(){r==null||r()},[])};o.Z=e},92770:function(E,o,t){t.d(o,{mf:function(){return a}});var s=function(n){return n!==null&&typeof n=="object"},a=function(n){return typeof n=="function"},f=function(n){return typeof n=="string"},e=function(n){return typeof n=="boolean"},m=function(n){return typeof n=="number"},r=function(n){return typeof n=="undefined"}},31663:function(E,o){var t=!1;o.Z=t},65223:function(E,o,t){t.d(o,{RV:function(){return i},Rk:function(){return n},Ux:function(){return P},aM:function(){return d},q3:function(){return m},qI:function(){return r}});var s=t(87462),a=t(71990),f=t(98423),e=t(67294),m=e.createContext({labelAlign:"right",vertical:!1,itemRef:function(){}}),r=e.createContext(null),i=function(u){var c=(0,f.Z)(u,["prefixCls"]);return e.createElement(a.RV,(0,s.Z)({},c))},n=e.createContext({prefixCls:""}),d=e.createContext({}),P=function(u){var c=u.children,x=u.status,M=u.override,p=(0,e.useContext)(d),h=(0,e.useMemo)(function(){var l=(0,s.Z)({},p);return M&&delete l.isFormItemInput,x&&(delete l.status,delete l.hasFeedback,delete l.feedbackIcon),l},[x,M,p]);return e.createElement(d.Provider,{value:h},c)}}}]);
