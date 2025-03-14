#  Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import numpy as np

TABLE_TYPE_NEXT_VALUE_SEPARATOR = '__pydev_table_column_type_val__'
MAX_COLWIDTH = 100000

ONE_DIM, TWO_DIM, WITH_TYPES = range(3)
NP_ROWS_TYPE = "int64"

is_pd = False
try:
    import pandas as pd
    version = pd.__version__
    majorVersion = int(version[0])
    pd.set_option('display.max_colwidth', None)
    is_pd = majorVersion>=1
    is_pd = True
except:
    pass


def get_type(arr):
    # type: (np.ndarray) -> str
    return str(type(arr))


def get_shape(arr):
    # type: (np.ndarray) -> str
    return str(arr.shape[0])


def get_head(arr):
    # type: (np.ndarray) -> str
    return repr(_create_table(arr).head().to_html(notebook=True, max_cols=None))


def get_column_types(arr):
    # type: (np.ndarray) -> str
    table = _create_table(arr)
    cols_types = [str(t) for t in table.dtypes] if is_pd else table.get_cols_types()

    return NP_ROWS_TYPE + TABLE_TYPE_NEXT_VALUE_SEPARATOR + \
        TABLE_TYPE_NEXT_VALUE_SEPARATOR.join(cols_types)


def get_data(arr, start_index=None, end_index=None):
    # type: (Union[np.ndarray, dict], int, int) -> str
    def convert_data_to_html(data, max_cols):
        return repr(_create_table(data, start_index, end_index).to_html(notebook=True, max_cols=max_cols))

    return _compute_data(arr, convert_data_to_html)


def display_data(arr, start_index=None, end_index=None):
    # type: (np.ndarray, int, int) -> None
    def ipython_display(data, max_cols):
        from IPython.display import display, HTML
        display(HTML(_create_table(data, start_index, end_index).to_html(notebook=True, max_cols=max_cols)))

    _compute_data(arr, ipython_display)


class _NpTable:
    def __init__(self, np_array):
        self.array = np_array
        self.type = self.get_array_type()
        self.indexes = None

    def get_array_type(self):
        col_type = self.array.dtype

        if len(col_type) != 0:
            return WITH_TYPES

        if self.array.ndim > 1:
            return TWO_DIM

        return ONE_DIM

    def get_cols_types(self):
        col_type = self.array.dtype

        if self.type == ONE_DIM:
            # [1, 2, 3] -> [int]
            return [str(col_type)]

        if self.type == WITH_TYPES:
            # ([(10, 3.14), (20, 2.71)], dtype=[("ci", "i4"), ("cf", "f4")]) -> [int, float]
            return [str(col_type[i]) for i in range(len(col_type))]  # is not iterable

        # [[1, 2], [3, 4]] -> [int, int]
        return [str(col_type) for _ in range(len(self.array[0]))]

    def head(self):
        if self.array.shape[0] < 6:
            return self

        return _NpTable(self.array[:5]).sort()

    def to_html(self, notebook, max_cols):
        html = ['<table class="dataframe">\n']

        # columns names
        html.append('<thead>\n'
                    '<tr style="text-align: right;">\n'
                    '<th></th>\n')
        html += self._collect_cols_names()
        html.append('</tr>\n'
                    '</thead>\n')

        # tbody
        html += self._collect_values(max_cols)

        html.append('</table>\n')

        return "".join(html)

    def _collect_cols_names(self):
        if self.type == ONE_DIM:
            return ['<th>0</th>\n']

        if self.type == WITH_TYPES:
            return ['<th>{}</th>\n'.format(name) for name in self.array.dtype.names]

        return ['<th>{}</th>\n'.format(i) for i in range(len(self.array[0]))]

    def _collect_values(self, max_cols):
        html = ['<tbody>\n']
        rows = self.array.shape[0]
        for row_num in range(rows):
            html.append('<tr>\n')
            html.append('<th>{}</th>\n'.format(int(self.indexes[row_num])))
            if self.type == ONE_DIM:
                html.append('<td>{}</td>\n'.format(self.array[row_num]))
            else:
                cols = len(self.array[0])
                max_cols = cols if max_cols is None else min(max_cols, cols)
                for col_num in range(max_cols):
                    html.append('<td>{}</td>\n'.format(self.array[row_num][col_num]))
            html.append('</tr>\n')
        html.append('</tbody>\n')
        return html

    def slice(self, start_index=None, end_index=None):
        if end_index is not None and start_index is not None:
            self.array = self.array[start_index:end_index]
            self.indexes = self.indexes[start_index:end_index]

        return self

    def sort(self, sort_keys=None):
        self.indexes = np.arange(self.array.shape[0])
        if sort_keys is None:
            return self

        cols, orders = sort_keys
        if 0 in cols:
            return self._sort_by_index(True in orders)

        if self.type == ONE_DIM:
            extended = np.column_stack((self.indexes, self.array))
            sort_extended = extended[:, 1].argsort()
            if False in orders:
                sort_extended = sort_extended[::-1]
            result = extended[sort_extended]
            self.array = result[:, 1]
            self.indexes = result[:, 0]
            return self

        if self.type == WITH_TYPES:
            new_dt = np.dtype([('_pydevd_i', 'i8')] + self.array.dtype.descr)
            extended = np.zeros(self.array.shape, dtype=new_dt)
            extended['_pydevd_i'] = list(range(self.array.shape[0]))
            for col in self.array.dtype.names:
                extended[col] = self.array[col]

            column_names = self.array.dtype.names
            for i in range(len(cols) - 1, -1, -1):
                name = column_names[cols[i] - 1]
                sort = extended[name].argsort(kind='stable')
                extended = extended[sort if orders[i] else sort[::-1]]
            self.indexes = extended['_pydevd_i']
            for col in self.array.dtype.names:
                self.array[col] = extended[col]
            return self

        extended = np.insert(self.array, 0, self.indexes, axis=1)
        for i in range(len(cols) - 1, -1, -1):
            sort = extended[:, cols[i]].argsort(kind='stable')
            extended = extended[sort if orders[i] else sort[::-1]]
        self.indexes = extended[:, 0]
        self.array = extended[:, 1:]
        return self

    def _sort_by_index(self, order):
        if order:
            return self
        self.array = self.array[::-1]
        self.indexes = self.indexes[::-1]
        return self


def _sort_df(dataframe, sort_keys):
    if sort_keys is None:
        return dataframe

    cols, orders = sort_keys
    if 0 in cols:
        if len(cols) == 1:
            return dataframe.sort_index(ascending=orders[0])
        return dataframe.sort_index(level=cols, ascending=orders)
    sort_by = list(map(lambda c: dataframe.columns[c - 1], cols))
    return dataframe.sort_values(by=sort_by, ascending=orders)


def _create_table(command, start_index=None, end_index=None):
    sort_keys = None

    if type(command) is dict:
        np_array = command['data']
        sort_keys = command['sort_keys']
    else:
        np_array = command

    try:
        import tensorflow as tf
        if isinstance(np_array, tf.SparseTensor):
            np_array = tf.sparse.to_dense(tf.sparse.reorder(np_array))
    except ImportError:
        pass
    try:
        import torch
        if isinstance(np_array, torch.Tensor):
            np_array = np_array.to_dense()
    except ImportError:
        pass

    if is_pd:
        sorting_arr = _sort_df(pd.DataFrame(np_array), sort_keys)
        if start_index is not None and end_index is not None:
            return sorting_arr.iloc[start_index:end_index]
        return sorting_arr

    return _NpTable(np_array).sort(sort_keys).slice(start_index, end_index)


def _compute_data(arr, fun):
    is_sort_command = type(arr) is dict
    data = arr['data'] if is_sort_command else arr

    jb_max_cols, jb_max_colwidth, jb_max_rows = None, None, None
    if is_pd:
        jb_max_cols, jb_max_colwidth, jb_max_rows = _set_pd_options()

    if is_sort_command:
        arr['data'] = data
        data = arr

    data = fun(data, None)

    if is_pd:
        _reset_pd_options(jb_max_cols, jb_max_colwidth, jb_max_rows)

    return data


def __get_tables_display_options():
    # type: () -> Tuple[None, Union[int, None], None]
    import sys
    if sys.version_info < (3, 0):
        return None, MAX_COLWIDTH, None
    return None, None, None


def _set_pd_options():
    max_cols, max_colwidth, max_rows = __get_tables_display_options()

    _jb_max_cols = pd.get_option('display.max_columns')
    _jb_max_colwidth = pd.get_option('display.max_colwidth')
    _jb_max_rows = pd.get_option('display.max_rows')

    pd.set_option('display.max_columns', max_cols)
    pd.set_option('display.max_rows', max_rows)
    try:
        pd.set_option('display.max_colwidth', max_colwidth)
    except ValueError:
        pd.set_option('display.max_colwidth', MAX_COLWIDTH_PYTHON_2)

    return _jb_max_cols, _jb_max_colwidth, _jb_max_rows


def _reset_pd_options(max_cols, max_colwidth, max_rows):
    pd.set_option('display.max_columns', max_cols)
    pd.set_option('display.max_colwidth', max_colwidth)
    pd.set_option('display.max_rows', max_rows)
