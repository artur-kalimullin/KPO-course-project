import { Pagination } from "react-bootstrap";

export const CustomPagination = ({ page, totalPages, onPageChange }) => {
    if (totalPages <= 1) return null;

    let items = [];
    for (let number = 1; number <= totalPages; number++) {
        items.push(
            <Pagination.Item 
                key={number} 
                active={number === page} 
                onClick={() => onPageChange(number)}
            >
                {number}
            </Pagination.Item>
        );
    }
    return <Pagination className="justify-content-center mt-3">{items}</Pagination>;
};